#ifndef ES_RESULT_QUEUE
#define ES_RESULT_QUEUE

#include <queue>
#include <mutex>
#include "es_semaphore.h"

#define QUEUE_TIMEOUT 20 // milliseconds

struct ESResult;

class ESResultQueue {
    public:
        ESResultQueue(unsigned int capacity);
        ~ESResultQueue();

        void clear();
        bool pop(unsigned int timeout_ms, ESResult*& result);
        bool push(unsigned int timeout_ms, ESResult* result);

    private:
        std::queue<ESResult*> m_queue;
        std::mutex m_queue_mutex;
        es_semaphore m_push_semaphore;
        es_semaphore m_pop_semaphore;
};

#endif
