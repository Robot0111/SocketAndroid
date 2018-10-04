<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
<head>
<title>WebSocket Application</title>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width">
<script type="text/javascript" src="js/jquery.min.js"></script>
<link
    href='css/m.css'
    rel='stylesheet' type='text/css'>
<link href="css/style.css" type="text/css" rel='stylesheet' />
<script type="text/javascript" src="js/main.js"></script>
</head>
<body>
    <div class="body_container">

        <div id="header">
            <h1>Android WebSockets</h1>
            <p class='online_count'>
                <b>23</b> 在线人数
            </p>
        </div>

        <div id="prompt_name_container" class="box_shadow">
            <p>请输入昵称</p>
            <form id="form_submit" method="post">
                <input type="text" id="input_name" /> <input type="submit"
                    value="JOIN" id="btn_join">
            </form>
        </div>

        <div id="message_container" class="box_shadow">

            <ul id="messages">
            </ul>


            <div id="input_message_container">
                <form id="form_send_message" method="post" action="#">
                    <input type="text" id="input_message"
                        placeholder="Type your message here..." /> <input type="submit"
                        id="btn_send" onclick="send();" value="发送" />
                    <div class="clear"></div>
                </form>
            </div>
            <div>

                <input type="button" onclick="closeSocket();"
                    value="离开聊天室" id="btn_close" />
            </div>

        </div>

    </div>

</body>
</html>
