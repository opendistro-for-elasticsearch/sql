#ifndef ES_SEMAPHORE
#define ES_SEMAPHORE

#ifdef WIN32
  #include <windows.h>
#elif defined(__APPLE__)
  #include <dispatch/dispatch.h>
#else 
  #include <semaphore.h>
#endif

class es_semaphore {
    public:
        es_semaphore(unsigned int initial, unsigned int capacity);
        ~es_semaphore();

        void lock();
        void release();
        bool try_lock_for(unsigned int timeout_ms);

    private:
#ifdef WIN32
        HANDLE m_semaphore;
#elif defined(__APPLE__)
        dispatch_semaphore_t m_semaphore;
#else 
        sem_t m_semaphore;
#endif
};

#endif
