package com.franzliszt.magicmusic.route.login.pwdlogin

import com.franzliszt.magicmusic.bean.pwdlogin.PwdLoginBean

sealed class PwdLoginStatus(val msg:String) {
    object LoginEmpty:PwdLoginStatus("账号和密码不能为空!")
    object LoginSuccess:PwdLoginStatus("登录成功!")
    object LoginFailed:PwdLoginStatus("登录失败,账号或密码错误!")
    object ForgetPassword:PwdLoginStatus("忘记密码!")
    object GoogleLogin:PwdLoginStatus("暂未实现Google登录，请切换其他登录方式！")
    object TwitterLogin:PwdLoginStatus("暂未实现Twitter登录，请切换其他登录方式！")
    data class NetworkFailed(val message:String):PwdLoginStatus(message)
}