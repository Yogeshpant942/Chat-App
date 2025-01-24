package com.example.chatappreal.Notification

class Token {
     var token :String= ""

    constructor(){}

    constructor(token:String){
        this.token = token
    }
    fun getToken():String{
        return token
    }
    fun setToken(token: String){
        this.token  = token
    }
}