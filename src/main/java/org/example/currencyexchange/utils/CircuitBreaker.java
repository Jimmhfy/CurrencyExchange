package org.example.currencyexchange.utils;

import org.example.currencyexchange.exception.CircuitBreakerException;

/*
    # Overview

    Implement a Circuit Breaker pattern to prevent cascading failures when calling external services.
    The circuit breaker counts failures and stops calling a failing service after a threshold is reached.

    ## Requirements

    Implement a circuit breaker with two states:

    - Passing (default): Function calls pass through normally
    - Blocking: Function calls are blocked, returning immediate errors

    ## Functionality:

    - Accept a function to protect and a failure threshold
    - Track consecutive failures
    - When passing: Execute the function and return its result
    - When failures reach threshold: Switch to blocking state
    - When blocking: Return errors without calling the function

    ## Basic Usage Example:

    ``` pseudo
    val circuitBreaker = CircuitBreaker(callExternalService, failureThreshold = 3)
    val result = circuitBreaker.execute(parameters)
    ```
*/
public class CircuitBreaker
{
    private CircuitBreakerState state;
    public enum CircuitBreakerState {
        OPEN,
        HALF_OPEN,
        CLOSE
    }
    private int thresholdLimit = 0;
    private int consecutiveFailures = 0;

    CircuitBreaker(int thresholdLimit) {
        state = CircuitBreakerState.CLOSE;
        this.thresholdLimit = thresholdLimit;
    }

    public <R> R execute(FunctionalSupplier<R> function) {
        if (state == CircuitBreakerState.OPEN) throw new CircuitBreakerException("Circuit Breaker Opened!");
        try {
            R output = function.apply();
            consecutiveFailures = 0;
            return output;
        } catch (Exception e) {
            consecutiveFailures++;
            if (consecutiveFailures >= thresholdLimit) {
                state = CircuitBreakerState.OPEN;
                throw new CircuitBreakerException("Circuit Breaker Just Opened!");
            }
            throw new CircuitBreakerException("Circuit Breaker is not meet the threshold!");
        }
    }

    public <I,R> R execute(FunctionalInterfaceUtil<I,R> function, I input) {
        return execute(() -> function.apply(input));
    }
}

/*
1. 狀態恢復機制：半熔斷狀態 (Half-Open State)
你之前寫嘅版本（以及规格书要求）只有 PASSING 同 BLOCKING。但真實世界嘅熔斷器，在爆開（BLOCKING）一段時間後，必須要能夠自動嘗試恢復！
原本想問但冇問到： 「當 Circuit Breaker 變咗 BLOCKING 之後，5 分鐘後有新 Request 入嚟，你點樣利用多線程定時器（Scheduler）或者時間戳（Timestamp），將佢放寬去 HALF-OPEN 狀態，試放一條 Request 過去睇吓個外部服務好返未？」
2. 併發狀態下的「錯誤吞吐率與滑動窗口」 (Sliding Window Strategy)
投行高併發環境絕對不會只用簡單的「連續錯 3 次就熔斷」（Consecutive Failures），因為如果 1 秒內有 1 萬個 Request 成功，只有 3 個失敗，根本不需要熔斷。
原本想問但冇問到： 「你點樣引入計時器或者一個 Queue，做到『在過去 10 秒之內，失敗率超過 50% 才觸發熔斷』？如果 100 條 Thread 同時塞進來，你點保證這個滑動窗口（Sliding Window）的計數是準確的？」
3. 多型擴展：非同步/異步呼叫的保護 (CompletableFuture / Reactive)
現實中投行的交易系統很多都是異步非阻塞（Asynchronous）的，不會傻傻地等一個 Function.apply() 慢慢行。
原本想問但冇問到： 「如果我的外部服務回傳的是一個 CompletableFuture<R> 或者 Spring WebFlux 嘅 Mono<R>，你原本那個 execute 方法會一瞬間直接跑完，根本捉不到裡面的異步錯誤。你點樣重構個 Circuit Breaker 去支持非同步生態？」
4. 監控與指標度量 (Metrics & Telemetry)
在生產環境，熔斷器開了還是關了，營運團隊（SRE）必須即時知道。
原本想問但冇問到： 「你點樣喺狀態轉移（State Transition）嗰一刻，引入事件監聽器（Event Listener）或者微米度量（Micrometer），將熔斷指標即時推送到 Prometheus / Grafana 看板？」
 */

/*
1. Rate Limiter (限流器) —— 出場率 90%
同熔斷器一樣，用嚟保護系統唔會被一瞬間嘅流量衝垮，或者限制某個 API 用戶每秒只能 call 100 次。
面試點考： 叫你手寫一個限流演算法。
核心演算法（一定要識）：
Token Bucket (權杖桶)： 固定速率派 Token，Request 嚟到攞到 Token 先行得。
Leaky Bucket (漏桶)： 流量好似水咁注入桶，以固定速率漏出嚟處理。
多線程考點： 點樣唔用 synchronized（效能太差），而純利用「時間戳差值計算」加 AtomicLong 嚟做到無鎖（Lock-free）計數？

2. LRU Cache (最近最少使用快取) —— 經典必考題
要求你設計一個記憶體快取（Cache），有固定容量（Capacity），當 Cache 爆滿而又有新數據入嚟嗰陣，要自動刪除最耐冇被讀寫過（Least Recently Used）嘅數據。
面試點考： 實作 get(key) 同 put(key, value)，兩個 Method 嘅時間複雜度都必須係 O(1)。
底層架構： 必須結合 HashMap（做到 O(1) 查找） + Double LinkedList（雙向鏈表，做到 O(1) 搬移節點）。
多線程考點： Java 原生嘅 LinkedHashMap 唔係 Thread-safe。如果多條 Thread 同時 put，點樣確保雙向鏈表嘅指標（head / tail / next / prev）唔會斷開、唔會行到死循環？

3. Thread Pool (線程池) 內部元件 / 任務排程器 (Task Scheduler)
投行極度依賴非同步交易，所以對線程調度要求極高。面試官會叫你手寫一個迷你版嘅 Task Executor。
面試點考： 「設計一個定時任務調度器，傳入一個 Runnable 同埋一個延遲時間（Delay），時間到就搵 Thread 行佢。」
底層架構： 需要用到 PriorityBlockingQueue（優先級阻塞佇列），根據執行時間排序，再加一條固定嘅 Worker Thread 去 take() 任務。
多線程考點： 點樣處理 Thread.interrupt()？當 Queue 冇任務嗰陣，點樣令條 Thread 優雅地 wait() 休息，有任務入嚟嗰陣又點樣 notify() 喚醒佢（生產者消費者模型 Producer-Consumer）？

4. Connection Pool (資料庫/網絡連接池) —— 寫過就知底細
好似 HikariCP 咁，建立一條連線去 DB 好貴（要 TCP 三次握手），所以要一開機就 new 埋一堆起度，有 Request 嚟就借出去，用完就還返返嚟。
面試點考： 實作 getConnection() 同 releaseConnection(conn)。
底層架構： 用兩個集合，一個放空閒連線（idleConnections），一個放用緊嘅連線（busyConnections）。
多線程考點： 如果連線池空咗（借晒出去），新一條 Thread 嚟到，你點樣令佢安全地等（Blocking Block with Timeout）？當有人還車（連線）嗰陣，點樣即刻放行等緊嘅
 */
