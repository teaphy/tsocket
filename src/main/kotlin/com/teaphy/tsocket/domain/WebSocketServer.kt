package com.teaphy.tsocket.domain

import io.reactivex.rxjava3.core.Flowable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import javax.websocket.*
import javax.websocket.server.ServerEndpoint


@ServerEndpoint(value = "/ws/asset")
@Component
class WebSocketServer {
    @PostConstruct
    fun init() {
        println("websocket 加载")
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    fun onOpen(session: Session) {
        SessionSet.add(session)
        val cnt = OnlineCount.incrementAndGet() // 在线数加1
        log.info("有连接加入，当前连接数为：{}", cnt)
        SendMessage(session, "连接成功")

        Flowable.interval(3, TimeUnit.SECONDS)
                .subscribe {
                    val time = LocalTime.now()
                    SendMessage(session, "当前时间：$time")
                }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    fun onClose(session: Session?) {
        SessionSet.remove(session)
        val cnt = OnlineCount.decrementAndGet()
        log.info("有连接关闭，当前连接数为：{}", cnt)
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message
     * 客户端发送过来的消息
     */
    @OnMessage
    fun onMessage(message: String, session: Session?) {
        log.info("来自客户端的消息：{}", message)
        SendMessage(session!!, "收到消息，消息内容：$message")
    }

    /**
     * 出现错误
     * @param session
     * @param error
     */
    @OnError
    fun onError(session: Session, error: Throwable) {
        log.error("发生错误：{}，Session ID： {}", error.message, session.getId())
        error.printStackTrace()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WebSocketServer::class.java)
        private val OnlineCount = AtomicInteger(0)

        // concurrent包的线程安全Set，用来存放每个客户端对应的Session对象。
        private val SessionSet: CopyOnWriteArraySet<Session> = CopyOnWriteArraySet<Session>()

        /**
         * 发送消息，实践表明，每次浏览器刷新，session会发生变化。
         * @param session
         * @param message
         */
        fun SendMessage(session: Session, message: String?) {
            try {
                session.getBasicRemote().sendText(java.lang.String.format("%s (From Server，Session ID=%s)", message, session.getId()))
            } catch (e: IOException) {
                log.error("发送消息出错：{}", e.message)
                e.printStackTrace()
            }
        }

        /**
         * 群发消息
         * @param message
         * @throws IOException
         */
        @Throws(IOException::class)
        fun BroadCastInfo(message: String?) {
            for (session in SessionSet) {
                if (session.isOpen()) {
                    SendMessage(session, message)
                }
            }
        }

        /**
         * 指定Session发送消息
         * @param sessionId
         * @param message
         * @throws IOException
         */
        @Throws(IOException::class)
        fun SendMessage(message: String?, sessionId: String?) {
            var session: Session? = null
            for (s in SessionSet) {
                if (s.getId().equals(sessionId)) {
                    session = s
                    break
                }
            }
            if (session != null) {
                SendMessage(session, message)
            } else {
                log.warn("没有找到你指定ID的会话：{}", sessionId)
            }
        }
    }
}