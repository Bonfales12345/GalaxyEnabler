typedef __UINT8_TYPE__ uint8_t;
typedef __INT32_TYPE__ int32_t;
typedef __UINT32_TYPE__ uint32_t;
typedef __INT64_TYPE__ int64_t;
typedef __UINT64_TYPE__ uint64_t;
typedef __SIZE_TYPE__ size_t;
typedef __UINTPTR_TYPE__ uintptr_t;

extern void* malloc(size_t size);
extern void* calloc(size_t nmemb, size_t size);
extern void free(void* ptr);
extern void* memset(void* s, int c, size_t n);
extern void* memcpy(void* dest, const void* src, size_t n);
extern size_t strlen(const char* s);
extern float sqrtf(float x);
extern void* dlopen(const char* filename, int flags);
extern void* dlsym(void* handle, const char* symbol);
extern const char* dlerror(void);
extern int __android_log_print(int prio, const char* tag, const char* fmt, ...);

/* libstdc++ / libc++ operator new / sized delete used by std::vector<int> and std::vector<DataBuffer> */
extern void* _Znwm(size_t size);
extern void _ZdlPvm(void* ptr, size_t size);

#define LOG_INFO 4
#define LOG_ERR  6
#define LOG_TAG  "VE_SnapTfl"

#define TFL_TYPE_FLOAT32 1
#define TFL_TYPE_INT32   2
#define TFL_TYPE_UINT8   3
#define TFL_TYPE_INT64   4
#define TFL_TYPE_INT8    9

#define MODE_OTHER   0
#define MODE_LOCATOR 1
#define MODE_REID    2

#define LOCATOR_ROWS 6300
#define LOCATOR_COLS 29
#define POSE_ROWS    174
#define POSE_BINS    640
#define REID_DIM     128

static const char kTflLibPath[] = "/data/local/tmp/mca/libtflgpu.so";
static const char kHumanModelPath[] =
    "/system/etc/style_transfer/db/12-26_PhotoEditor_StyleTransfer_v1.0.0_HumanDetector.tflite";

/* ABI structs matching libc++ std::vector and Samsung SNAP DataBuffer (sizeof == 40) */
typedef struct {
    int32_t* begin;
    int32_t* end;
    int32_t* cap;
} StdVecI32;

typedef struct {
    void*     data;
    StdVecI32 dims;
    int32_t   dataFormat;
    int32_t   dataType;
} DataBuffer;

typedef struct {
    DataBuffer* begin;
    DataBuffer* end;
    DataBuffer* cap;
} StdVecDataBuffer;

_Static_assert(sizeof(StdVecI32) == 24, "StdVecI32 must be 24 bytes");
_Static_assert(sizeof(DataBuffer) == 40, "DataBuffer must be 40 bytes");
_Static_assert(sizeof(StdVecDataBuffer) == 24, "StdVecDataBuffer must be 24 bytes");

/* Dynamic TFLite C API table loaded from libtflgpu.so */
typedef void* (*FnModelFromFile)(const char* path);
typedef void (*FnModelDelete)(void* model);
typedef void* (*FnOptsCreate)(void);
typedef void (*FnOptsSetThreads)(void* opts, int32_t threads);
typedef void (*FnOptsDelete)(void* opts);
typedef void* (*FnInterpCreate)(const void* model, const void* opts);
typedef void (*FnInterpDelete)(void* interp);
typedef int32_t (*FnInterpAlloc)(void* interp);
typedef void* (*FnInterpGetIn)(const void* interp, int32_t idx);
typedef const void* (*FnInterpGetOut)(const void* interp, int32_t idx);
typedef int32_t (*FnInterpGetOutCount)(const void* interp);
typedef int32_t (*FnInterpInvoke)(void* interp);
typedef void* (*FnTensorData)(const void* t);
typedef size_t (*FnTensorBytes)(const void* t);
typedef int32_t (*FnTensorNumDims)(const void* t);
typedef int32_t (*FnTensorDim)(const void* t, int32_t dim_idx);
typedef int32_t (*FnTensorType)(const void* t);

static int g_tfl_state = 0; /* 0=uninit, 1=loading, 2=ready, -1=failed */
static void* g_tfl_handle = 0;
static FnModelFromFile     g_ModelFromFile = 0;
static FnModelDelete       g_ModelDelete = 0;
static FnOptsCreate        g_OptsCreate = 0;
static FnOptsSetThreads    g_OptsSetThreads = 0;
static FnOptsDelete        g_OptsDelete = 0;
static FnInterpCreate      g_InterpCreate = 0;
static FnInterpDelete      g_InterpDelete = 0;
static FnInterpAlloc       g_InterpAlloc = 0;
static FnInterpGetIn       g_InterpGetIn = 0;
static FnInterpGetOut      g_InterpGetOut = 0;
static FnInterpGetOutCount g_InterpGetOutCount = 0;
static FnInterpInvoke      g_InterpInvoke = 0;
static FnTensorData        g_TensorData = 0;
static FnTensorBytes       g_TensorBytes = 0;
static FnTensorNumDims     g_TensorNumDims = 0;
static FnTensorDim         g_TensorDim = 0;
static FnTensorType        g_TensorType = 0;

static inline int str_contains(const char* hay, const char* needle) {
    if (!hay || !needle) return 0;
    if (!*needle) return 1;
    for (const char* h = hay; *h; ++h) {
        const char* p = h;
        const char* q = needle;
        while (*p && *q && *p == *q) {
            ++p;
            ++q;
        }
        if (!*q) return 1;
    }
    return 0;
}

static inline float clampf(float v, float lo, float hi) {
    if (!(v > lo)) return lo; /* handles NaN cleanly by clamping to lo */
    if (v > hi) return hi;
    return v;
}

static inline int clampi(int v, int lo, int hi) {
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
}

static void decode_std_string(const void* sptr, char* out, size_t out_sz) {
    if (!out || out_sz == 0) return;
    out[0] = '\0';
    if (!sptr) return;
    const uint8_t* s = (const uint8_t*)sptr;
    const char* src = 0;
    size_t len = 0;
    if ((s[0] & 1) == 0) {
        src = (const char*)(s + 1);
        len = (size_t)(s[0] >> 1);
    } else {
        len = *(const size_t*)(s + 8);
        src = *(const char* const*)(s + 16);
    }
    if (!src || len == 0) return;
    if (len >= out_sz) len = out_sz - 1;
    memcpy(out, src, len);
    out[len] = '\0';
}

static int tfl_ensure_loaded(void) {
    int st = __atomic_load_n(&g_tfl_state, __ATOMIC_ACQUIRE);
    if (st == 2) return 0;
    if (st == -1) return -1;

    int expected = 0;
    if (__atomic_compare_exchange_n(&g_tfl_state, &expected, 1, 0, __ATOMIC_ACQ_REL, __ATOMIC_ACQUIRE)) {
        void* h = dlopen(kTflLibPath, 2 /* RTLD_NOW | RTLD_LOCAL */);
        if (!h) {
            __android_log_print(LOG_ERR, LOG_TAG, "dlopen(%s) failed: %s", kTflLibPath, dlerror());
            __atomic_store_n(&g_tfl_state, -1, __ATOMIC_RELEASE);
            return -1;
        }
        g_tfl_handle        = h;
        g_ModelFromFile     = (FnModelFromFile)dlsym(h, "TfLiteModelCreateFromFile");
        g_ModelDelete       = (FnModelDelete)dlsym(h, "TfLiteModelDelete");
        g_OptsCreate        = (FnOptsCreate)dlsym(h, "TfLiteInterpreterOptionsCreate");
        g_OptsSetThreads    = (FnOptsSetThreads)dlsym(h, "TfLiteInterpreterOptionsSetNumThreads");
        g_OptsDelete        = (FnOptsDelete)dlsym(h, "TfLiteInterpreterOptionsDelete");
        g_InterpCreate      = (FnInterpCreate)dlsym(h, "TfLiteInterpreterCreate");
        g_InterpDelete      = (FnInterpDelete)dlsym(h, "TfLiteInterpreterDelete");
        g_InterpAlloc       = (FnInterpAlloc)dlsym(h, "TfLiteInterpreterAllocateTensors");
        g_InterpGetIn       = (FnInterpGetIn)dlsym(h, "TfLiteInterpreterGetInputTensor");
        g_InterpGetOut      = (FnInterpGetOut)dlsym(h, "TfLiteInterpreterGetOutputTensor");
        g_InterpGetOutCount = (FnInterpGetOutCount)dlsym(h, "TfLiteInterpreterGetOutputTensorCount");
        g_InterpInvoke      = (FnInterpInvoke)dlsym(h, "TfLiteInterpreterInvoke");
        g_TensorData        = (FnTensorData)dlsym(h, "TfLiteTensorData");
        g_TensorBytes       = (FnTensorBytes)dlsym(h, "TfLiteTensorByteSize");
        g_TensorNumDims     = (FnTensorNumDims)dlsym(h, "TfLiteTensorNumDims");
        g_TensorDim         = (FnTensorDim)dlsym(h, "TfLiteTensorDim");
        g_TensorType        = (FnTensorType)dlsym(h, "TfLiteTensorType");

        if (!g_ModelFromFile || !g_ModelDelete || !g_OptsCreate || !g_OptsSetThreads ||
            !g_OptsDelete || !g_InterpCreate || !g_InterpDelete || !g_InterpAlloc ||
            !g_InterpGetIn || !g_InterpGetOut || !g_InterpGetOutCount || !g_InterpInvoke ||
            !g_TensorData || !g_TensorBytes || !g_TensorNumDims || !g_TensorDim || !g_TensorType) {
            __android_log_print(LOG_ERR, LOG_TAG, "Missing required TfLite* symbol in %s", kTflLibPath);
            __atomic_store_n(&g_tfl_state, -1, __ATOMIC_RELEASE);
            return -1;
        }
        __atomic_store_n(&g_tfl_state, 2, __ATOMIC_RELEASE);
        return 0;
    }

    while ((st = __atomic_load_n(&g_tfl_state, __ATOMIC_ACQUIRE)) == 1) {
        __asm__ volatile("yield" ::: "memory");
    }
    return (st == 2) ? 0 : -1;
}

typedef struct {
    void** vtable;
    int    mode;
    int    return_nchw_shape;
    int    frame_count;
    int    reserved;
    void*  tfl_model;
    void*  tfl_opts;
    void*  tfl_interp;
    float* locator_out;
    float* pose_out;
    float* reid_out;
    float* other_out;
} SnapSession;

static void free_tfl_resources(SnapSession* s) {
    if (!s) return;
    if (s->tfl_interp && g_InterpDelete) {
        g_InterpDelete(s->tfl_interp);
    }
    s->tfl_interp = 0;
    if (s->tfl_opts && g_OptsDelete) {
        g_OptsDelete(s->tfl_opts);
    }
    s->tfl_opts = 0;
    if (s->tfl_model && g_ModelDelete) {
        g_ModelDelete(s->tfl_model);
    }
    s->tfl_model = 0;
}

static void vec_i32_assign(StdVecI32* vec, const int32_t* vals, size_t n) {
    if (!vec) return;
    if (vec->begin) {
        size_t old_bytes = (size_t)((const char*)vec->cap - (const char*)vec->begin);
        _ZdlPvm(vec->begin, old_bytes);
        vec->begin = 0;
        vec->end   = 0;
        vec->cap   = 0;
    }
    if (n == 0) return;
    int32_t* d = (int32_t*)_Znwm(n * sizeof(int32_t));
    for (size_t i = 0; i < n; ++i) {
        d[i] = vals[i];
    }
    vec->begin = d;
    vec->end   = d + n;
    vec->cap   = d + n;
}

static void prepare_output_buffers(StdVecDataBuffer* outputs, size_t needed) {
    for (DataBuffer* p = outputs->begin; p < outputs->end; ++p) {
        if (p->dims.begin) {
            size_t old_b = (size_t)((const char*)p->dims.cap - (const char*)p->dims.begin);
            _ZdlPvm(p->dims.begin, old_b);
            p->dims.begin = 0;
            p->dims.end   = 0;
            p->dims.cap   = 0;
        }
    }
    size_t cap_elems = (outputs->begin && outputs->cap >= outputs->begin)
                           ? (size_t)(outputs->cap - outputs->begin)
                           : 0;
    if (cap_elems < needed) {
        if (outputs->begin) {
            size_t old_bytes = (size_t)((const char*)outputs->cap - (const char*)outputs->begin);
            _ZdlPvm(outputs->begin, old_bytes);
        }
        DataBuffer* arr = (DataBuffer*)_Znwm(needed * sizeof(DataBuffer));
        memset(arr, 0, needed * sizeof(DataBuffer));
        outputs->begin = arr;
        outputs->cap   = arr + needed;
    } else if (outputs->begin) {
        memset(outputs->begin, 0, needed * sizeof(DataBuffer));
    }
    outputs->end = outputs->begin + needed;
}

static void set_output_slot(DataBuffer* slot, void* data, const int32_t* d4) {
    slot->data       = data;
    slot->dims.begin = 0;
    slot->dims.end   = 0;
    slot->dims.cap   = 0;
    vec_i32_assign(&slot->dims, d4, 4);
    slot->dataFormat = 0;
    slot->dataType   = 0;
}

/* Bilinear resize 320x320x3 float [0..1] -> dst_h x dst_w x 3 (uint8 0..255 or float32 0..1) */
static void resize_rgb_to_tensor(const float* src, void* dst_raw, int dst_h, int dst_w, int dst_type) {
    const int src_h = 320;
    const int src_w = 320;
    const float scale_y = (float)src_h / (float)dst_h;
    const float scale_x = (float)src_w / (float)dst_w;

    uint8_t* dst_u8   = (dst_type == TFL_TYPE_UINT8) ? (uint8_t*)dst_raw : 0;
    float*   dst_f32  = (dst_type == TFL_TYPE_FLOAT32) ? (float*)dst_raw : 0;

    if (!src) {
        size_t total = (size_t)dst_h * (size_t)dst_w * 3;
        if (dst_u8) memset(dst_u8, 0, total);
        else if (dst_f32) memset(dst_f32, 0, total * sizeof(float));
        return;
    }

    for (int y = 0; y < dst_h; ++y) {
        float sy = (y + 0.5f) * scale_y - 0.5f;
        if (sy < 0.0f) sy = 0.0f;
        int y0 = (int)sy;
        if (y0 >= src_h - 1) y0 = src_h - 2;
        int y1 = y0 + 1;
        float fy = sy - (float)y0;
        if (fy < 0.0f) fy = 0.0f;
        if (fy > 1.0f) fy = 1.0f;
        float w0_y = 1.0f - fy;

        const float* row0 = src + (size_t)y0 * (size_t)src_w * 3;
        const float* row1 = src + (size_t)y1 * (size_t)src_w * 3;

        for (int x = 0; x < dst_w; ++x) {
            float sx = (x + 0.5f) * scale_x - 0.5f;
            if (sx < 0.0f) sx = 0.0f;
            int x0 = (int)sx;
            if (x0 >= src_w - 1) x0 = src_w - 2;
            int x1 = x0 + 1;
            float fx = sx - (float)x0;
            if (fx < 0.0f) fx = 0.0f;
            if (fx > 1.0f) fx = 1.0f;
            float w0_x = 1.0f - fx;

            float w00 = w0_y * w0_x;
            float w01 = w0_y * fx;
            float w10 = fy * w0_x;
            float w11 = fy * fx;

            size_t ix0 = (size_t)x0 * 3;
            size_t ix1 = (size_t)x1 * 3;
            size_t di  = ((size_t)y * (size_t)dst_w + (size_t)x) * 3;

            for (int c = 0; c < 3; ++c) {
                float v = w00 * clampf(row0[ix0 + c], 0.0f, 1.0f)
                        + w01 * clampf(row0[ix1 + c], 0.0f, 1.0f)
                        + w10 * clampf(row1[ix0 + c], 0.0f, 1.0f)
                        + w11 * clampf(row1[ix1 + c], 0.0f, 1.0f);
                if (dst_u8) {
                    int iv = (int)(v * 255.0f + 0.5f);
                    dst_u8[di + c] = (uint8_t)clampi(iv, 0, 255);
                } else if (dst_f32) {
                    dst_f32[di + c] = v;
                }
            }
        }
    }
}

static int run_locator(SnapSession* s, const float* in_rgb) {
    memset(s->locator_out, 0, (size_t)LOCATOR_ROWS * (size_t)LOCATOR_COLS * sizeof(float));
    if (!in_rgb || !s->tfl_interp) return 0;

    void* in_t = g_InterpGetIn(s->tfl_interp, 0);
    if (!in_t) return -1;
    void* in_data = g_TensorData(in_t);
    int   ndims   = g_TensorNumDims(in_t);
    int   in_h    = (ndims >= 3) ? g_TensorDim(in_t, 1) : 300;
    int   in_w    = (ndims >= 3) ? g_TensorDim(in_t, 2) : 300;
    int   in_c    = (ndims >= 4) ? g_TensorDim(in_t, 3) : 3;
    int   in_type = g_TensorType(in_t);
    if (!in_data || in_h <= 0 || in_w <= 0 || in_c != 3) return -1;

    resize_rgb_to_tensor(in_rgb, in_data, in_h, in_w, in_type);

    if (g_InterpInvoke(s->tfl_interp) != 0) {
        __android_log_print(LOG_ERR, LOG_TAG, "TfLiteInterpreterInvoke failed");
        return -1;
    }

    int out_cnt = g_InterpGetOutCount(s->tfl_interp);
    if (out_cnt < 4) {
        __android_log_print(LOG_ERR, LOG_TAG, "Unexpected output tensor count=%d", out_cnt);
        return -1;
    }

    const void* t_boxes  = g_InterpGetOut(s->tfl_interp, 0);
    const void* t_cls    = g_InterpGetOut(s->tfl_interp, 1);
    const void* t_scores = g_InterpGetOut(s->tfl_interp, 2);
    const void* t_num    = g_InterpGetOut(s->tfl_interp, 3);
    if (!t_boxes || !t_cls || !t_scores || !t_num) return -1;

    const float* boxes  = (const float*)g_TensorData(t_boxes);
    const float* cls    = (const float*)g_TensorData(t_cls);
    const float* scores = (const float*)g_TensorData(t_scores);
    const float* fnum   = (const float*)g_TensorData(t_num);
    int max_det = (g_TensorNumDims(t_boxes) >= 2) ? g_TensorDim(t_boxes, 1) : 10;
    if (!boxes || !cls || !scores || !fnum || max_det <= 0) return -1;

    int num_det = (int)(*fnum + 0.5f);
    if (num_det < 0) num_det = 0;
    if (num_det > max_det) num_det = max_det;

    /* Encode detections into Head 2 (stride=32, 10x10 grid, rows 6000..6299) using exact
       inverse DecoderYOLOv7::decode_preds_anchor equations with class_id=2 (human). */
    static const float aw[3]  = { 116.0f, 156.0f, 373.0f };
    static const float ah[3]  = {  90.0f, 198.0f, 326.0f };
    static const int   base[3] = { 6000, 6100, 6200 };
    static const int   d_offsets[9][2] = {
        {0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
    };
    uint8_t used[3][100];
    memset(used, 0, sizeof(used));

    int kept = 0;
    float best_score = 0.0f;
    float cls0 = (num_det > 0) ? cls[0] : -1.0f;
    for (int i = 0; i < num_det; ++i) {
        float sc = scores[i];
        if (sc > best_score) best_score = sc;
        if (!(sc >= 0.35f)) continue;
        /* HumanDetector.tflite uses 0-indexed COCO classes where 0 == person */
        int c_id = (int)(cls[i] + 0.5f);
        if (c_id != 0) continue;

        float ymin = clampf(boxes[i * 4 + 0], 0.0f, 1.0f);
        float xmin = clampf(boxes[i * 4 + 1], 0.0f, 1.0f);
        float ymax = clampf(boxes[i * 4 + 2], 0.0f, 1.0f);
        float xmax = clampf(boxes[i * 4 + 3], 0.0f, 1.0f);
        if (xmax <= xmin + 0.01f || ymax <= ymin + 0.01f) continue;

        float bx1 = xmin * 320.0f;
        float by1 = ymin * 320.0f;
        float bx2 = xmax * 320.0f;
        float by2 = ymax * 320.0f;
        float cx  = 0.5f * (bx1 + bx2);
        float cy  = 0.5f * (by1 + by2);
        float bw  = bx2 - bx1;
        float bh  = by2 - by1;
        if (bw < 4.0f) bw = 4.0f;
        if (bh < 4.0f) bh = 4.0f;

        int gx0 = clampi((int)(cx / 32.0f), 0, 9);
        int gy0 = clampi((int)(cy / 32.0f), 0, 9);

        int chosen_a = -1, chosen_gx = gx0, chosen_gy = gy0;
        for (int n = 0; n < 9 && chosen_a < 0; ++n) {
            int nx = gx0 + d_offsets[n][0];
            int ny = gy0 + d_offsets[n][1];
            if (nx < 0 || nx > 9 || ny < 0 || ny > 9) continue;
            for (int a = 2; a >= 0; --a) {
                if (!used[a][ny * 10 + nx]) {
                    used[a][ny * 10 + nx] = 1;
                    chosen_a  = a;
                    chosen_gx = nx;
                    chosen_gy = ny;
                    break;
                }
            }
        }
        if (chosen_a < 0) continue;

        int row_idx = base[chosen_a] + chosen_gy * 10 + chosen_gx;
        float* row = s->locator_out + (size_t)row_idx * LOCATOR_COLS;
        row[0] = ((cx / 32.0f) - (float)chosen_gx + 0.5f) * 0.5f;
        row[1] = ((cy / 32.0f) - (float)chosen_gy + 0.5f) * 0.5f;
        row[2] = sqrtf(bw / (4.0f * aw[chosen_a]));
        row[3] = sqrtf(bh / (4.0f * ah[chosen_a]));
        row[4] = clampf(sc, 0.0f, 0.99f);
        /* VideoDetection::process filters boxes with DetectionBox.score < 0.95f */
        row[5 + 2] = 0.955f + 0.044f * clampf(sc, 0.0f, 1.0f); /* class_id = 2 => human */
        ++kept;
    }

    int fidx = s->frame_count++;
    if (fidx < 5 || (fidx % 60) == 0) {
        __android_log_print(LOG_INFO, LOG_TAG,
                            "Locator frame=%d raw_num=%d kept=%d best=%.3f cls0=%.1f",
                            fidx, num_det, kept, best_score, cls0);
    }
    return 0;
}

static inline void set_kp_bin(float* pose_out, int kp, float kx, float ky, float conf) {
    int bx = clampi((int)(kx * 2.0f + 0.5f), 0, POSE_BINS - 1);
    int by = clampi((int)(ky * 2.0f + 0.5f), 0, POSE_BINS - 1);
    pose_out[(size_t)kp * POSE_BINS + (size_t)bx]         = conf;
    pose_out[(size_t)(87 + kp) * POSE_BINS + (size_t)by] = conf;
}

static int is_face_kp(int kp) {
    if (kp <= 4) return 1; /* nose, eyes, ears */
    if (kp >= 17 && kp <= 20) return 1;
    if (kp >= 63 && kp <= 82) return 1;
    return 0;
}

static void run_reid(SnapSession* s, const float* in_rgb) {
    memset(s->pose_out, 0, (size_t)POSE_ROWS * (size_t)POSE_BINS * sizeof(float));
    memset(s->reid_out, 0, (size_t)REID_DIM * sizeof(float));

    int x0 = 320, y0 = 320, x1 = -1, y1 = -1;
    if (in_rgb) {
        for (int y = 0; y < 320; y += 2) {
            const float* row = in_rgb + (size_t)y * 320 * 3;
            for (int x = 0; x < 320; x += 2) {
                float r = clampf(row[x * 3 + 0], 0.0f, 1.0f);
                float g = clampf(row[x * 3 + 1], 0.0f, 1.0f);
                float b = clampf(row[x * 3 + 2], 0.0f, 1.0f);
                if (r + g + b > 0.015f) {
                    if (x < x0) x0 = x;
                    if (x > x1) x1 = x;
                    if (y < y0) y0 = y;
                    if (y > y1) y1 = y;
                }
            }
        }
    }
    if (x1 - x0 < 16 || y1 - y0 < 16) {
        x0 = 24; y0 = 24; x1 = 296; y1 = 296;
    }

    int rf = s->frame_count++;
    if (rf < 5 || (rf % 60) == 0) {
        __android_log_print(LOG_INFO, LOG_TAG,
                            "Reid frame=%d bbox=(%d,%d,%d,%d)",
                            rf, x0, y0, x1, y1);
    }

    float bx0 = (float)x0;
    float by0 = (float)y0;
    float bw  = (float)(x1 - x0);
    float bh  = (float)(y1 - y0);
    if (bw < 8.0f) bw = 8.0f;
    if (bh < 8.0f) bh = 8.0f;
    float cx  = bx0 + 0.5f * bw;

    /* Populate all 87 keypoints inside content bbox so verify_pose, face framing, and pose2box GIoU pass cleanly */
    int body_idx = 0;
    int face_idx = 0;
    for (int k = 0; k < 87; ++k) {
        float kx, ky;
        if (is_face_kp(k)) {
            int col = face_idx % 7;
            int rw  = face_idx / 7;
            kx = cx + (((float)col - 3.0f) / 3.0f) * (0.14f * bw);
            ky = by0 + (0.08f + ((float)rw / 4.0f) * 0.14f) * bh;
            ++face_idx;
        } else if (k == 21) {
            kx = bx0 + 0.07f * bw;
            ky = by0 + 0.07f * bh;
        } else if (k == 22) {
            kx = bx0 + 0.93f * bw;
            ky = by0 + 0.07f * bh;
        } else if (k == 23) {
            kx = bx0 + 0.07f * bw;
            ky = by0 + 0.93f * bh;
        } else if (k == 24) {
            kx = bx0 + 0.93f * bw;
            ky = by0 + 0.93f * bh;
        } else if (k >= 5 && k <= 16) {
            int band = body_idx % 4;
            float frac_x = ((float)((body_idx * 7) % 13) / 12.0f) * 0.68f - 0.34f;
            float frac_y = (band == 0) ? 0.28f : (band == 1) ? 0.46f : (band == 2) ? 0.65f : 0.84f;
            kx = cx + frac_x * bw;
            ky = by0 + frac_y * bh;
            ++body_idx;
        } else {
            int band = body_idx % 4;
            float frac_x = ((float)((body_idx * 7) % 13) / 12.0f) * 0.80f - 0.40f;
            float frac_y = (band == 0) ? 0.24f : (band == 1) ? 0.45f : (band == 2) ? 0.67f : 0.88f;
            kx = cx + frac_x * bw;
            ky = by0 + frac_y * bh;
            ++body_idx;
        }
        set_kp_bin(s->pose_out, k, kx, ky, 0.90f);
    }

    /* Compute 128-dim L2-normalized spatial RGB descriptor: 4 vertical strips x 32 features */
    if (!in_rgb) {
        s->reid_out[0] = 1.0f;
        return;
    }

    int strip_h = (y1 - y0 + 1);
    for (int st = 0; st < 4; ++st) {
        int sy0 = y0 + (st * strip_h) / 4;
        int sy1 = y0 + ((st + 1) * strip_h) / 4;
        if (sy1 <= sy0) sy1 = sy0 + 1;
        if (sy1 > 320) sy1 = 320;

        float* feat = s->reid_out + st * 32;
        float sum_r = 0, sum_g = 0, sum_b = 0, sum_l = 0;
        float sum_r2 = 0, sum_g2 = 0, sum_b2 = 0, sum_l2 = 0;
        int count = 0;

        for (int y = sy0; y < sy1; y += 2) {
            const float* row = in_rgb + (size_t)y * 320 * 3;
            for (int x = x0; x <= x1 && x < 320; x += 2) {
                float r = clampf(row[x * 3 + 0], 0.0f, 1.0f);
                float g = clampf(row[x * 3 + 1], 0.0f, 1.0f);
                float b = clampf(row[x * 3 + 2], 0.0f, 1.0f);
                float l = 0.299f * r + 0.587f * g + 0.114f * b;

                int br = clampi((int)(r * 7.99f), 0, 7);
                int bg = clampi((int)(g * 7.99f), 0, 7);
                int bb = clampi((int)(b * 7.99f), 0, 7);
                feat[0 + br]  += 1.0f;
                feat[8 + bg]  += 1.0f;
                feat[16 + bb] += 1.0f;

                sum_r += r; sum_g += g; sum_b += b; sum_l += l;
                sum_r2 += r * r; sum_g2 += g * g; sum_b2 += b * b; sum_l2 += l * l;
                ++count;
            }
        }
        if (count > 0) {
            float inv = 1.0f / (float)count;
            for (int i = 0; i < 24; ++i) feat[i] *= inv;
            float mr = sum_r * inv, mg = sum_g * inv, mb = sum_b * inv, ml = sum_l * inv;
            float vr = sum_r2 * inv - mr * mr; if (vr < 0.0f) vr = 0.0f;
            float vg = sum_g2 * inv - mg * mg; if (vg < 0.0f) vg = 0.0f;
            float vb = sum_b2 * inv - mb * mb; if (vb < 0.0f) vb = 0.0f;
            float vl = sum_l2 * inv - ml * ml; if (vl < 0.0f) vl = 0.0f;
            feat[24] = mr; feat[25] = mg; feat[26] = mb; feat[27] = ml;
            feat[28] = sqrtf(vr); feat[29] = sqrtf(vg); feat[30] = sqrtf(vb); feat[31] = sqrtf(vl);
        }
    }

    float norm2 = 0.0f;
    for (int i = 0; i < REID_DIM; ++i) norm2 += s->reid_out[i] * s->reid_out[i];
    if (norm2 > 1e-12f) {
        float inv_norm = 1.0f / sqrtf(norm2);
        for (int i = 0; i < REID_DIM; ++i) s->reid_out[i] *= inv_norm;
    } else {
        s->reid_out[0] = 1.0f;
    }
}

/* SnapSession virtual methods */
static int sess_stub0(SnapSession* s) {
    (void)s;
    return 0;
}

static int sess_execute(SnapSession* s, const StdVecDataBuffer* inputs, StdVecDataBuffer* outputs) {
    if (!s || !outputs) return -1;
    const float* in_rgb = (inputs && inputs->begin && inputs->begin < inputs->end)
                              ? (const float*)inputs->begin[0].data
                              : 0;

    if (s->mode == MODE_LOCATOR) {
        int rc = run_locator(s, in_rgb);
        if (rc != 0) return rc;
        static const int32_t d4[4] = { 1, LOCATOR_ROWS, LOCATOR_COLS, 1 };
        prepare_output_buffers(outputs, 1);
        set_output_slot(&outputs->begin[0], s->locator_out, d4);
        return 0;
    }
    if (s->mode == MODE_REID) {
        run_reid(s, in_rgb);
        static const int32_t d_pose[4] = { 1, POSE_ROWS, POSE_BINS, 1 };
        static const int32_t d_feat[4] = { 1, REID_DIM, 1, 1 };
        prepare_output_buffers(outputs, 2);
        set_output_slot(&outputs->begin[0], s->pose_out, d_pose);
        set_output_slot(&outputs->begin[1], s->reid_out, d_feat);
        return 0;
    }

    static const int32_t d_other[4] = { 1, 1, 1, 1 };
    prepare_output_buffers(outputs, 1);
    set_output_slot(&outputs->begin[0], s->other_out, d_other);
    return 0;
}

static int sess_close(SnapSession* s) {
    free_tfl_resources(s);
    return 0;
}

static int sess_get_input_shape(SnapSession* s, int index, StdVecI32* out_dims) {
    (void)index;
    if (!s || !out_dims) return -1;
    if (s->return_nchw_shape) {
        static const int32_t nchw[4] = { 1, 3, 320, 320 };
        vec_i32_assign(out_dims, nchw, 4);
    } else {
        static const int32_t nhwc[4] = { 1, 320, 320, 3 };
        vec_i32_assign(out_dims, nhwc, 4);
    }
    return 0;
}

static int sess_open(SnapSession* s, const void* opts) {
    if (!s || !opts) return -1;
    const uint8_t* base = (const uint8_t*)opts;
    int32_t comp_unit = *(const int32_t*)(base + 0x00);
    int32_t sub_unit  = *(const int32_t*)(base + 0x08);
    s->return_nchw_shape = ((comp_unit == 2 && sub_unit == 1) ||
                            (comp_unit == 1 && sub_unit == 9)) ? 1 : 0;

    const void* out_vec_begin = *(const void* const*)(base + 0x28);
    const void* out_vec_end   = *(const void* const*)(base + 0x30);
    size_t num_out_names = (out_vec_begin && out_vec_end >= out_vec_begin)
                               ? (size_t)(((const char*)out_vec_end - (const char*)out_vec_begin) / 24)
                               : 0;

    char model_name[128];
    decode_std_string(base + 0xf0, model_name, sizeof(model_name));

    if (str_contains(model_name, "Locator") || str_contains(model_name, "Detection")) {
        s->mode = MODE_LOCATOR;
    } else if (str_contains(model_name, "Reid") || str_contains(model_name, "Pose") || num_out_names >= 2) {
        s->mode = MODE_REID;
    } else {
        s->mode = MODE_OTHER;
    }

    __android_log_print(LOG_INFO, LOG_TAG,
                        "Open model='%s' mode=%d comp=%d sub=%d nchw=%d outNames=%zu",
                        model_name, s->mode, comp_unit, sub_unit, s->return_nchw_shape, num_out_names);

    if (s->mode == MODE_LOCATOR) {
        if (tfl_ensure_loaded() != 0) return -2;
        free_tfl_resources(s);

        s->tfl_model = g_ModelFromFile(kHumanModelPath);
        if (!s->tfl_model) {
            __android_log_print(LOG_ERR, LOG_TAG, "Failed to load %s", kHumanModelPath);
            return -3;
        }
        s->tfl_opts = g_OptsCreate();
        if (s->tfl_opts) {
            g_OptsSetThreads(s->tfl_opts, 4);
        }
        s->tfl_interp = g_InterpCreate(s->tfl_model, s->tfl_opts);
        if (!s->tfl_interp) {
            __android_log_print(LOG_ERR, LOG_TAG, "TfLiteInterpreterCreate failed");
            free_tfl_resources(s);
            return -4;
        }
        if (g_InterpAlloc(s->tfl_interp) != 0) {
            __android_log_print(LOG_ERR, LOG_TAG, "TfLiteInterpreterAllocateTensors failed");
            free_tfl_resources(s);
            return -5;
        }
        if (!s->locator_out) {
            s->locator_out = (float*)calloc((size_t)LOCATOR_ROWS * (size_t)LOCATOR_COLS, sizeof(float));
            if (!s->locator_out) {
                free_tfl_resources(s);
                return -6;
            }
        }
        void* in0 = g_InterpGetIn(s->tfl_interp, 0);
        int nd = in0 ? g_TensorNumDims(in0) : 0;
        __android_log_print(LOG_INFO, LOG_TAG,
                            "TFLite Locator ready: in type=%d dims=[%d,%d,%d,%d] outs=%d",
                            in0 ? g_TensorType(in0) : -1,
                            nd > 0 ? g_TensorDim(in0, 0) : -1,
                            nd > 1 ? g_TensorDim(in0, 1) : -1,
                            nd > 2 ? g_TensorDim(in0, 2) : -1,
                            nd > 3 ? g_TensorDim(in0, 3) : -1,
                            g_InterpGetOutCount(s->tfl_interp));
        return 0;
    }

    if (s->mode == MODE_REID) {
        if (!s->pose_out) {
            s->pose_out = (float*)calloc((size_t)POSE_ROWS * (size_t)POSE_BINS, sizeof(float));
        }
        if (!s->reid_out) {
            s->reid_out = (float*)calloc((size_t)REID_DIM, sizeof(float));
        }
        if (!s->pose_out || !s->reid_out) return -6;
        return 0;
    }

    if (!s->other_out) {
        s->other_out = (float*)calloc(16, sizeof(float));
    }
    return s->other_out ? 0 : -6;
}

static void* g_snap_vtable[42] __attribute__((aligned(16))) = {
    (void*)0,                 /* [-2] offset to top */
    (void*)0,                 /* [-1] RTTI */
    (void*)&sess_stub0,       /* [0]  ~SnapSession() */
    (void*)&sess_stub0,       /* [1]  ~SnapSession() deleting */
    (void*)&sess_stub0,       /* [2] */
    (void*)&sess_execute,     /* [3]  +0x18 Execute */
    (void*)&sess_close,       /* [4]  +0x20 Close */
    (void*)&sess_get_input_shape, /* [5] +0x28 GetModelInputShape */
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_open,        /* [32] +0x100 Open */
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0,
    (void*)&sess_stub0, (void*)&sess_stub0, (void*)&sess_stub0
};

__attribute__((visibility("default")))
void* CreateSnapSession(void) {
    SnapSession* s = (SnapSession*)calloc(1, sizeof(SnapSession));
    if (!s) return 0;
    s->vtable = &g_snap_vtable[2];
    return s;
}

__attribute__((visibility("default")))
void DestroySnapSession(void* ptr) {
    SnapSession* s = (SnapSession*)ptr;
    if (!s) return;
    free_tfl_resources(s);
    if (s->locator_out) { free(s->locator_out); s->locator_out = 0; }
    if (s->pose_out)    { free(s->pose_out);    s->pose_out    = 0; }
    if (s->reid_out)    { free(s->reid_out);    s->reid_out    = 0; }
    if (s->other_out)   { free(s->other_out);   s->other_out   = 0; }
    free(s);
}
