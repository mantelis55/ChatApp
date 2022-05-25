import React, { useEffect, useRef, useState} from "react";
import { useNavigate } from "react-router-dom";
import { Button } from 'react-bootstrap';

export default function ChatPage() {
    const navigate = useNavigate();
    const webSocket = useRef(null);
    const chatInput = useRef()
    const recieverInput = useRef()
    const [leftContent, setLeftContent] = useState([]);
    const [rightContent, setRightContent] = useState([]);
    const [usersContent, setUsersContent] = useState([]);
    const [sessionID, setSessionID] = useState("");
    let rightContentRef = useRef();
    let leftContentRef = useRef();

    useEffect(() => {
        if(sessionStorage.getItem("loggedIn") !== null) {
            navigate("/");
        }
        else{
            sessionStorage.setItem("loggedIn", "true");

            webSocket.current = new WebSocket('ws://localhost:8080/ChatServer/ws');
            webSocket.current.onopen = () => {
                webSocket.current.send(sessionStorage.getItem("token"));
                console.log('WebSocket Client Connected');
            };
        
            webSocket.current.onmessage = (message) => {
                const json = JSON.parse(message.data);
                if(json.purpose === "SESSIONID"){
                    setSessionID(json.message);
                }
                else if (json.purpose === "USERLIST"){
                    updateUserList();
                }
                else{
                    setRightContent(rightContentRef.current + json.message + "\n");
                    setLeftContent(leftContentRef.current + getNewLineCount(json.message));
                }
            };
            webSocket.current.onerror = function() {
                console.log('Connection Error');
            };
        }
        // eslint-disable-next-line
    }, []);

    useEffect(() => {
        rightContentRef.current = rightContent;
    }, [rightContent])

    useEffect(() => {
        leftContentRef.current = leftContent;
        var chatDiv = document.getElementById("chatForm");
        chatDiv.scrollTo(0,chatDiv.scrollHeight)
    }, [leftContent])

    function messageToAll(e){
        const messageText = chatInput.current.value
        setLeftContent(leftContentRef.current + "YOU: " + messageText + "\n");
        setRightContent(rightContentRef.current + getNewLineCount(messageText));
        chatInput.current.value = "";
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({ "message":  sessionStorage.getItem('token') + ": " + messageText, "sessionID": sessionID, "sender": sessionStorage.getItem('token') })
        };
        fetch('http://localhost:8080/ChatServer/messages/all', requestOptions)
        .then(res => res.text())
        .then(res => console.log(res))
    }

    function updateUserList(e){
        const requestOptions = {
            method: 'GET'
        };
        fetch('http://localhost:8080/ChatServer/users/loggedIn', requestOptions)
        .then(res => res.json())
        .then(data => setUsersContent(data.message));
    }

    function privateMessage(e){
        const messageText = chatInput.current.value
        chatInput.current.value = "";
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({ "message":  sessionStorage.getItem('token') + ": " + messageText, "sessionID": sessionID, "sender": sessionStorage.getItem('token'), "reciever": recieverInput.current.value})
        };

        fetch('http://localhost:8080/ChatServer/messages/user', requestOptions)
        .then(res => {if(res.status === 200) {setLeftContent(leftContentRef.current + "YOU: " + messageText + "\n");
        setRightContent(rightContentRef.current + getNewLineCount(messageText));} else {setLeftContent(leftContentRef.current +  "Cannot send message to user - " + recieverInput.current.value + "\n");
        setRightContent(rightContentRef.current + getNewLineCount("Cannot send message to user - " + recieverInput.current.value));}})
    }

    function registerMessage(e){
        if(recieverInput.current.value === ""){
            messageToAll(e);
        }
        else privateMessage(e);
    }

    function logOut(e){
        const requestOptions = {
            method: 'DELETE',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({"sessionID": sessionID})
        };
        fetch('http://localhost:8080/ChatServer/users/loggedIn/' + sessionStorage.getItem("token"), requestOptions)
        .then(res => {if(res.status === 200) navigate("/")});
    }

    function getNewLineCount(text){
        let newLineCount = text.length / 25;
        var newlines = "";
        for(var i = 0; i < newLineCount; i++){
            newlines += "\n"
        }
        return newlines;
    }

    return (
        <>
            <div className="centerLeft">
                <div className="logo"><b>ONLINE</b></div>
                <div className="usersForm" id="usersForm">
                    <b><p className="textUsers">{usersContent}</p></b>
                </div>
                <Button className="loginButton btn-secondary" onClick={logOut}>Logout</Button>
            </div>

            <div className="centerMiddle">
                <div className="logo"><b>CHAT</b></div>
                <div className="chatForm" id="chatForm">
                    <b><p className="textLeft">{leftContent}</p></b>
                    <b><p className="textRight">{rightContent}</p></b>
                </div>    
            </div>
            <div className="chattingFunctionalities">
                <div className="sideBySide">
                    <input id="chatInput" className="form-control inputs inputChat" ref={chatInput} placeholder="Chat here!" required/> 
                    <input className="form-control inputs inputReciever" ref={recieverInput} placeholder="All" required/> 
                </div>
                <Button className="loginButton btn-secondary" onClick={registerMessage}>Send</Button>
            </div>
        </>
    )
}




