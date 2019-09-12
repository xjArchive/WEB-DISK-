<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>网盘</title>
<script src="js/jquery-3.4.1.min.js"></script>
		<link rel="stylesheet" type="text/css" href="layui/css/layui.css" />
		<script type="text/javascript" src="layui/layui.js"></script>
		<link rel="stylesheet" type="text/css" href="css/login.css">
		<script src="js/login.js"></script>
</head>
<body>
	<div class="main">
			<div class="bg">
				<div class="layui-tab layui-tab-brief cover" lay-filter="docDemoTabBrief">
					<ul class="layui-tab-title">
						<li class="layui-this">登录</li>
						<li>注册</li>
						<li>更多</li>
					</ul>
					<div class="layui-tab-content">
						<div class="layui-tab-item layui-show">
							<div class="lo">
								<form action="Login" method="post">
									用户名:<input type="text" placeholder="请输入用户名/手机号码" name="username" id="username"
									value="" onfocus="this.value = '';" onblur="if (this.value == '') {this.value = '';}" /></br>
									密&nbsp;&nbsp;&nbsp;码:<input type="password" placeholder="请输入登录密码" name="password" id="password"
									value="" onfocus="this.value = '';" onblur="if (this.value == '') {this.value = '';}" /><br>
									验证码:<input id="inputCode" name="inputCode" type="text">
									<!-- 验证码 -->
									<label class="code" id="checkCode"></label>
									<a href="javascript:void(0)" id="look" style="color:blue;">看不清</a>
									<input type="checkbox" class="rem" checked="checked">记住密码
									<input type="submit" value="登录" class="btn layui-btn layui-anim layui-anim-rotate">
								</form>
							</div>
						</div>
						<div class="layui-tab-item">
							<div class="lo">
								<form action="register" method="post">
									用户名:<input type="text" placeholder="请输入用户名/手机号码" name="username" id="username" /></br>
									密&nbsp;&nbsp;&nbsp;码:<input type="password" placeholder="请输入登录密码" name="password" id="password" /><br>
									确认密码:<input id="rePassword" type="password" name="rePassword" value="" onfocus="this.value = '';" onblur="if (this.value == '') {this.value = '';}">
			
									邮&nbsp;&nbsp;&nbsp;箱:<input placeholder="请输入邮箱" id="email" type="text" name="email" value="" onfocus="this.value = '';" onblur="if (this.value == '') {this.value = '';}">
									
									<input type="submit" value="注册" class="btn layui-btn layui-anim layui-anim-rotate">
								</form>
							</div>
						</div>
						<div class="layui-tab-item">更多</div>
					</div>
				</div>
			</div>
		</div>
		<script>
			layui.use(['element','layer'], function() {
				var element = layui.element;
				var layer = layui.layer; 
				 var sysMsg = "${sysMsg}"
				if(sysMsg != "")
				layer.alert(sysMsg);
			});
			
		</script>
</body>
</html>