typedef __UINT8_TYPE__ uint8_t;
typedef __INT32_TYPE__ int32_t;
typedef __UINT64_TYPE__ uint64_t;
typedef __UINTPTR_TYPE__ uintptr_t;

typedef struct {
    const char* dli_fname;
    void* dli_fbase;
    const char* dli_sname;
    void* dli_saddr;
} Dl_info;

extern void* dlopen(const char* filename, int flags);
extern void* dlsym(void* handle, const char* symbol);
extern int dladdr(const void* addr, Dl_info* info);
extern int __android_log_print(int prio, const char* tag, const char* fmt, ...);

typedef void (*CreateBQFn)(void* outProducer, void* outConsumer, uint8_t consumerIsSurfaceFlinger);
typedef int32_t (*ReleaseBuf5Fn)(void* self, int32_t slot, uint64_t frameNumber,
                                 const void* releaseFence, void* display, void* fence);

static const char kSymCreateBQ[] = "_ZN7android11BufferQueue17createBufferQueueEPNS_2spINS_22IGraphicBufferProducerEEEPNS1_INS_22IGraphicBufferConsumerEEEb";

static int g_vtable_state = 0; // 0=uninit, 1=initializing, 2=ready
static void* g_patched_vtable[34] __attribute__((aligned(16)));
static ReleaseBuf5Fn g_real_release_buf5 = 0;
static int g_skip_log_cnt = 0;

static inline int contains_str(const char* hay, const char* needle) {
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

static int32_t releaseBuffer_A17_to_A16(void* self, int32_t slot, uint64_t frameNumber,
                                        const void* arg3, void* arg4, const void* arg5) {
    ReleaseBuf5Fn fn = __atomic_load_n(&g_real_release_buf5, __ATOMIC_ACQUIRE);
    if (!fn) return -38; // -ENOSYS
    if (((uintptr_t)arg3) < 0x1000ULL) {
        // Legacy A16 5-arg caller: arg3=EGLDisplay (0/small handle), arg4=EGLSyncKHR, arg5=const sp<Fence>&
        return fn(self, slot, frameNumber, arg5, (void*)arg3, arg4);
    }
    // A17 3-arg caller: arg3=const sp<Fence>&
    return fn(self, slot, frameNumber, arg3, (void*)0, (void*)0);
}

void _ZN7android11BufferQueue17createBufferQueueEPNS_2spINS_22IGraphicBufferProducerEEEPNS1_INS_22IGraphicBufferConsumerEEEb(
    void* outProducer, void* outConsumer, uint8_t consumerIsSurfaceFlinger) {
    void* caller_pc = (void*)(((uintptr_t)__builtin_return_address(0)) & 0x0000ffffffffffffULL);

    static CreateBQFn real_fn = 0;
    CreateBQFn fn = __atomic_load_n(&real_fn, __ATOMIC_ACQUIRE);
    if (!fn) {
        void* h = dlopen("libgui.so", 0x00002 | 0x00004); // RTLD_NOW | RTLD_NOLOAD
        if (!h) h = dlopen("libgui.so", 0x00002);
        if (h) fn = (CreateBQFn)dlsym(h, kSymCreateBQ);
        if (!fn) fn = (CreateBQFn)dlsym((void*)-1L, kSymCreateBQ); // RTLD_NEXT fallback
        if (fn) __atomic_store_n(&real_fn, fn, __ATOMIC_RELEASE);
    }
    if (!fn) {
        __android_log_print(6, "VEGuiShim", "Failed to resolve real createBufferQueue!");
        return;
    }

    fn(outProducer, outConsumer, consumerIsSurfaceFlinger);

    Dl_info info;
    info.dli_fname = 0;
    info.dli_fbase = 0;
    info.dli_sname = 0;
    info.dli_saddr = 0;
    int ok = dladdr(caller_pc, &info);
    if (!(ok && info.dli_fname && contains_str(info.dli_fname, "libveframework"))) {
        int idx = __atomic_fetch_add(&g_skip_log_cnt, 1, __ATOMIC_RELAXED);
        if (idx < 16) {
            __android_log_print(4, "VEGuiShim", "Skipping non-veframework caller=%p (%s)",
                                caller_pc, (ok && info.dli_fname) ? info.dli_fname : "unknown");
        }
        return;
    }

    if (outConsumer) {
        void* consumer = *(void**)outConsumer;
        if (consumer) {
            int state = __atomic_load_n(&g_vtable_state, __ATOMIC_ACQUIRE);
            if (state != 2) {
                int expected = 0;
                if (__atomic_compare_exchange_n(&g_vtable_state, &expected, 1, 0, __ATOMIC_ACQ_REL, __ATOMIC_ACQUIRE)) {
                    void** orig_vptr = *(void***)consumer;
                    void** base = orig_vptr - 2;
                    for (int i = 0; i < 34; ++i) {
                        g_patched_vtable[i] = base[i];
                    }
                    __atomic_store_n(&g_real_release_buf5, (ReleaseBuf5Fn)orig_vptr[29], __ATOMIC_RELEASE);
                    g_patched_vtable[2 + 9] = (void*)&releaseBuffer_A17_to_A16;
                    __android_log_print(4, "VEGuiShim",
                                        "Patched BufferQueueConsumer for caller=%p (%s) vtable=%p slot[11]=%p -> %p (slot[31]=%p)",
                                        caller_pc, info.dli_fname,
                                        orig_vptr, orig_vptr[9], (void*)&releaseBuffer_A17_to_A16, orig_vptr[29]);
                    __atomic_store_n(&g_vtable_state, 2, __ATOMIC_RELEASE);
                } else {
                    while (__atomic_load_n(&g_vtable_state, __ATOMIC_ACQUIRE) == 1) {
                    }
                }
            }
            if (__atomic_load_n(&g_vtable_state, __ATOMIC_ACQUIRE) == 2) {
                *(void***)consumer = &g_patched_vtable[2];
            }
        }
    }
}
