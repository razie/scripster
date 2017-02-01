
   // Inspired from a few places, like 
   // http://www.codelifter.com 
   // http://www.experts-exchange.com/Web_Development/Web_Languages-Standards/CSS/Q_23151088.html

// NOTE razInvoke defined in scripts.js, 

function razMove (m,x,y) {
   try {
     razInvoke ('/mutant/robot/mousemove?dx='+x+'&dy='+y+'&tstamp='+m)
   } catch(e) {}
}
function razPress (which,x,y) {
   try {
     razInvoke ('/mutant/robot/mousedown?which='+which+'&dx='+x+'&dy='+y)
   } catch(e) {}
}
function razRelease (which,x,y) {
   try {
     razInvoke ('/mutant/robot/mouseup?which='+which+'&dx='+x+'&dy='+y)
   } catch(e) {}
}

function razClick (which,x,y) {
   try {
     razInvoke ('/mutant/robot/mouseclick?which='+which+'&dx='+x+'&dy='+y)
   } catch(e) {}
}

function mp_lc() {razClick("left", 0, 0) }
function mp_ld() {razPress("left", 0, 0) }
function mp_lu() {razRelease("left", 0, 0) }
function mp_rc() {razClick("right", 0, 0) }
function mp_rd() {razPress("right", 0, 0) }
function mp_ru() {razRelease("right", 0, 0) }

var mp_mdown, mp_mup, mp_mmove;
var mp_tstart, mp_tend, mp_tmove;

function MM_setup () {
   var self = this;
   MM_setup2();
}

function MM_setup2 () {
   var self = this;

   this.VKI_isIE = /*@cc_on!@*/false;
   this.VKI_isIE6 = /*@if(@_jscript_version == 5.6)!@end@*/false;
   this.VKI_isIElt8 = /*@if(@_jscript_version < 5.8)!@end@*/false;
   this.VKI_isMoz = (navigator.product == "Gecko");
   this.VKI_isWebKit = RegExp("KHTML").test(navigator.userAgent);

   var mainc = document.createElement('div');
   document.body.appendChild(mainc);

   this.container = document.createElement('div');
   //container.style.position =  'absolute';
   this.container.id= 'mp_container';
   this.container.style.width =  '300px';
   this.container.style.height = '200px';
   this.container.style.left = '10px';   
   this.container.style.background = "darkgray";   
   this.container.style.top = '10px';   
   this.container.style.border = "5px solid navy";   
   //container.style.zIndex = 100;   
   //document.body.appendChild(this.container);
   mainc.appendChild(this.container);

   var newDiv = document.createElement('DIV');
   newDiv.style.position =  'absolute';
   newDiv.style.width =  '20px';
   newDiv.style.height = '20px';
   newDiv.style.left = '20px';   
   newDiv.style.top = '20px';   
   newDiv.style.border = "1px solid red";   
   newDiv.style.background = "red";   
   newDiv.style.zIndex = 100;   
   this.container.appendChild(newDiv)

   var c1 = document.createElement('div');
   c1.style.position =  'absolute';
   c1.id= 'mp_lcontainer';
   c1.style.width =  '50px';
   c1.style.height = '200px';
   c1.style.left = '320px';   
   c1.style.top = '10px';   
   c1.style.background = "darkgray";   
   mainc.appendChild(c1);
   var b1 = document.createElement('DIV');
   //b1.style.position =  'absolute';
   b1.style.width =  '50px';
   b1.style.height = '100px';
   b1.style.left = '0px';   
   b1.style.top = '0px';   
   b1.style.border = "1px solid red";   
   b1.style.background = "red";   
   b1.style.zIndex = 100;   
   c1.appendChild(b1)
   var b2 = document.createElement('DIV');
   //b2.style.position =  'absolute';
   b2.style.width =  '50px';
   b2.style.height = '100px';
   b2.style.left = '0px';   
   b2.style.top = '51px';   
   b2.style.border = "1px solid blue";   
   b2.style.background = "blue";   
   b2.style.zIndex = 100;   
   c1.appendChild(b2)

//if (!IE) document.captureEvents(Event.MOUSEMOVE)
//document.onmousemove = xy;

var tempX = 0;
var tempY = 0;
var lastMillis = 0;

function rMove(tempX, tempY, forced) {
   var x = (new Date()).getTime();
   if ((x-lastMillis) > 100 || forced) {
      lastMillis=x;

      newDiv.style.top=tempY+'px';
      newDiv.style.left=tempX+'px';

      var dx = tempX*1000/this.container.offsetWidth;
      var dy = tempY*1000/this.container.offsetHeight;

      razMove (lastMillis, dx, dy)
   }
}

function xy(e, forced) {
   if (this.VKI_isIE) { // grab the x-y pos.s if browser is IE
      tempX = event.clientX + document.body.scrollLeft;
      tempY = event.clientY + document.body.scrollTop;
   }
   else {  // grab the x-y pos.s if browser is NS
      tempX = e.pageX;
      tempY = e.pageY;
   }  

   if (tempX < 0){tempX = 0;}
   if (tempY < 0){tempY = 0;}  

   rMove (tempX, tempY, forced)
   }

mp_mmove = function rmMove (e) {
   e.preventDefault();
   xy(e, false)
}

mp_mdown = function rmDown (e) {
   e.preventDefault();
   document.getElementById('mp_container').onmousemove=mp_mmove;
   xy(e, true)
}

mp_mup = function rmUp (e) {
   document.getElementById('mp_container').onmousemove=null;
   xy(e, true)
}

document.getElementById('mp_container').onmousedown=mp_mdown;
document.getElementById('mp_container').onmouseup=mp_mup;

//b1.onmousedown=mp_ld;
//b1.onmouseup=mp_lu;
b1.onclick=mp_lc;
//b2.onmousedown=mp_rd;
//b2.onmouseup=mp_ru;
b2.onclick=mp_rc;

//------------------touch stuff on ipod

function txy (e, forced) {
   //curX = e.targetTouches[0].pageX - startX;
   //curY = e.targetTouches[0].pageY - startY;
   curX = e.targetTouches[0].pageX;
   curY = e.targetTouches[0].pageY;
   rMove(curX, curY, forced)
}

mp_tstart = function touchStart (e) {
   e.preventDefault();
   txy(e, true)
}

mp_tmove = function touchMove (e) {
   e.preventDefault();
   txy(e, false)
}

mp_tend = function touchEnd (e) {
   e.preventDefault();
   txy(e, true)
}

document.getElementById('mp_container').addEventListener("touchstart", mp_tstart, false);
document.getElementById('mp_container').addEventListener("touchmove", mp_tmove, false);
document.getElementById('mp_container').addEventListener("touchend", mp_tend, false);
//c.addEventListener("touchcancel", touchCancel, false);

//c.addEventListener("gesturestart", gestureStart, false);
//c.addEventListener("gesturechange", gestureChange, false);
//c.addEventListener("gestureend", gestureEnd, false);
}

  /* ***** Attach this script to the onload event ****************** */
  if (window.addEventListener) {
    window.addEventListener('load', MM_setup, false);
  } else if (window.attachEvent)
    window.attachEvent('onload', MM_setup);

