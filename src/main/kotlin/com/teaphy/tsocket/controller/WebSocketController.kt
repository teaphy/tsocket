package com.teaphy.tsocket.controller

import com.teaphy.tsocket.domain.WebSocketServer
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException


@RestController
@RequestMapping("/api/ws")
class WebSocketController {
    /**
     * 群发消息内容
     * @param message
     * @return
     */
    @RequestMapping(value = ["/sendAll"], method = [RequestMethod.GET])
    fun sendAllMessage(@RequestParam(required = true) message: String?): String {
        try {
            WebSocketServer.BroadCastInfo(message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "ok"
    }

    /**
     * 指定会话ID发消息
     * @param message 消息内容
     * @param id 连接会话ID
     * @return
     */
    @RequestMapping(value = ["/sendOne"], method = [RequestMethod.GET])
    fun sendOneMessage(@RequestParam(required = true) message: String?, @RequestParam(required = true) id: String?): String {
        try {
            WebSocketServer.SendMessage(message, id)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "ok"
    }
}