$(function() {
	var Disk = {
		init : function() {
			this.initdata();
			this.initevent();
		},
		initdata : function() {

			this.createCode();
		},

		//绑定事件
		initevent : function() {
			var current_this = this;
			current_this.changecode();
			//current_this.login();
			current_this.rem();
		},
		//记住密码
		rem : function() {
			//$(".rem").
		},
		changecode : function() {
			$(".code").click(function() {
				Disk.createCode();
			});
			$("#look").click(function() {
				Disk.createCode();
			});
		},
		createCode : function() {
			var code = "";
			var codeLength = 4;
			var checkCode = document.getElementById("checkCode");
			var codeChars = "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
			var charc = codeChars.split(",");
			for (var i = 0; i < codeLength; i++) {
				index = parseInt(Math.random() * charc.length);
				code += charc[index] + "  ";
				//随机改变字体颜色值
			}
			checkCode.innerHTML = code;
		},

	};
	Disk.init();
});
