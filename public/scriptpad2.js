

var editor = 0;
var canon = 0;

var razieCss="dark";
var razMode="ace/mode/scala";
var razTheme="ace/theme/twilight";

var SCRIP_RUN='/scripster/run?'

function darklight (css, dcss, lcss) {
  if (css == "light") return lcss;
  else return dcss;
}

function configIt (css,lang) {
  razieCss=css;

  razTheme=darklight (css, "ace/theme/twilight", "ace/theme/dawn")

  if (lang == 'scala') { razMode="ace/mode/scala"; }
  else if (lang == 'whatever') { }
}

// this will invoke the config above with the right css/language combination
razSetup();

function fromTextArea() {
  editor = ace.edit("code");
  editor.setTheme(razTheme); 
  var ScalaScriptMode = require("ace/mode/scala").Mode;
  editor.getSession().setMode(new ScalaScriptMode());
  editor.renderer.setHScrollBarAlwaysVisible(false)
  editor.renderer.setShowGutter(false)
  canon = require('pilot/canon')

  var mmm = document.getElementById("m1");

  canon.addCommand({
    name: 'F9-run',
    bindKey: { win: 'F9', mac: 'F9', sender: 'editor' },
    exec: function(env, args, request) {
            runLine(SCRIP_RUN)
    }
  })

  canon.addCommand({
    name: 'CF9-run',
    bindKey: { win: 'Ctrl-F9', mac: 'Command-F9', sender: 'editor' },
    exec: function(env, args, request) {
            runSelection(SCRIP_RUN)
    }
  })

  canon.addCommand({
    name: 'assist',
    bindKey: { win: 'Ctrl-Space', mac: 'Command-Space', sender: 'editor' },
    exec: function(env, args, request) {
            contentAssist("")
    }
  })

}

fromTextArea();

    window.onload = function() {
        setup2();
    };

function setup2 () {
//  editor.setUserKeyHandler (razspkey);
  mclose();
  statusx = document.getElementById('status');
  showStatus ("ready")
}

function razspkey (event) {
  if (event.keyCode == 120) { // F9
    if (event.ctrlKey) runSelection (SCRIP_RUN)
    else runLine(SCRIP_RUN)
  } else if (event.keyCode == 32 && event.ctrlKey)
     contentAssist(event)
}

function runLine (url) {
   var s1 = editor.getCursorPosition()
   var scr = editor.getSession().doc.getAllLines()[s1.row];

  showStatus ("sending line...")
   if (scr != null && scr.length > 0) 
     try {
       sendRequest(url+'language=scala&sessionId='+razSpSession+'&script='+urlescape(scr), showResult)
     } catch(e) {}
}

function runSelection (url) {
   var scr = editor.getSession().doc.getTextRange(editor.getSelectionRange());

   if (scr == null || scr.length == 0) scr = editor.getSession().getValue();
   
  showStatus ("sending selection...");
   try {
     sendRequest(url+'language=scala&sessionId='+razSpSession+'&script='+urlescape(scr), showResult)
   } catch(e) {}
}

function showStatus (msg) {
   try {
//  statusx.value=htmlBody("Status: " + msg);
  statusx = document.getElementById('status');
var ss = statusx;
  ss.firstChild.nodeValue=("Status: " + msg);
   } catch(e) {}
} 

function showResult (res) {
  var r = htmlBody(res.responseText);
  
  if (r.match("Scripster.Status:.*")) {
    showStatus (r.replace("Scripster.Status:", "..."));
    r = "";
    }
  else {
    showStatus ("got script result");
    }
    
  var txt = document.getElementById('result');
  txt.value=r;
} 

function replace(swhere,regexp,swhat) {
  var re = new RegExp(regexp, "g");
  return swhere.replace(re, swhat);
}

function htmlBody (text) {
   var s = replace (text, ".*<body[^>]*>", "");
   s = replace (s, "</body.*", "");
   return s;
}

function urlescape (s) {
    //   var ret = replace (escape(s), "\\+", "%2B");
   var ret1 = replace (encodeURI(s), "\\+", "%2B");
   return ret1;
}


//---------------- content assist

var currRequestCount = 0

function contentAssist (e) {
  mclose();
  
   var s1 = editor.getCursorPosition()
   var scr = editor.getSession().doc.getAllLines()[s1.row];
  currRequestCount += 1
  showStatus ("asking for content assist...")
  try {
    sendRequest('/scripster/options?sessionId='+razSpSession+'&pos='+s1.column+'&line='+urlescape(scr), 
                function (ret) {openWhenOptionsArrive(currRequestCount, e, ret)})
  } catch(e) {}
}

function openWhenOptionsArrive (reqCount, e, ret) {
  if (currRequestCount == reqCount) {
  try {
    var response = htmlBody(ret.responseText);
      var allitems = eval('(' + response + ')'); // TODO security risk?
      
      // use only the first... 10
      if (allitems.length > 10) {
        items = [];
        for (var k = 0; k < 10; k++) {
          items[k] = allitems[k];
          }
        items[10] = "...";
      } else {
        items = allitems
      }
      
      showStatus ("got content assist: " + allitems.length + " options");
      
   var s1 = editor.getCursorPosition()
   var scr = editor.getSession().doc.getAllLines()[s1.row];
     
      try { 
        mclose(); 
        var currLine = editor.getCursorPosition().row;
        var coll = editor.getCursorPosition().column;
   
        if(items != null && items[0] != undefined) {
          showMenu (currLine, scr, coll);
        } else {
          mclose();
        }
      } catch(e) {
        window.console.log (' >> ERROR:'+ e);
      }
    } catch(e) {
      window.console.log (' >> ERROR:'+ e);
    }
  }
  else 
  showStatus ("ignored content assist options - out of line")
}

// same code as TelnetReceiver.completion
function completion (curr, option) {
  if (curr == null || option == null || option == "" || curr.length <= 0 || curr.charAt(curr.length-1).match (/[.+-=:\*\/]/)) return option
  else {
         // find the last ID
         var i = curr.length-1
         while (i > 0 && curr.charAt(i).match("[a-zA-Z0-9]")) i -= 1
         var j = i+1
         if (i==0) j = i 
         var k = 0
         while (j < curr.length && k < option.length && curr.charAt(j) == option.charAt(k)) {
           j+=1; k +=1 
           }
         return option.substr(k, option.length)
      }
}

//------------------------- inspired from ... http://javascript-array.com/scripts/simple_drop_down_menu/

var timeout = 500;
var closetimer = 0;
var ddmenu = 0;
var items = []
var currSelected=0
var statusx = 0;

function buildMenu (currLine, line, column) {
   ddmenu = document.getElementById('m1');
   
   for (var k = 0; k < items.length; k++){
      var li  = document.createElement('a');
      var name = document.createTextNode (items[k]);
      li.appendChild (name);
      ddmenu.appendChild(li);
      if (k == 0) {
        currSelected = li
         currSelected.className = "razsel";   
      }
         
      li.onclick = li.doitman = function () {
        var cc = completion(line, this.text);
        editor.insert(cc);
        
//        var pos = editor.cursorPosition(true), text = editor.lineContent(pos.line);
 //       editor.selectLines(pos.line, text.length+1);

        // TODO problem: if you do content assist twice in a row, second time doesn't move to the end
      };
   }
}

// open hidden layer
function mopen() {  
   // cancel close timer
   mcancelclosetime();

   // close old layer
   if(ddmenu) ddmenu.style.visibility = 'hidden';

// you'd build new menu here...

   // get new layer and show it
   ddmenu= document.getElementById('m1');
   ddmenu.style.visibility = 'visible';
}

// close showed layer
function mclose() {
  try {
   ungrabKeys()
    } catch(e) {
    }

   if(ddmenu) {
     ddmenu.style.visibility = 'hidden';
     
     var listitems=ddmenu.getElementsByTagName("a")
     for (i=0; i<listitems.length; i++) 
       ddmenu.removeChild (listitems[i])
   }
  
   currSelected=0
}

// go close timer
function mclosetime() {
   closetimer = window.setTimeout(mclose, timeout);
}

// cancel close timer
function mcancelclosetime() {
   if (closetimer) {
      window.clearTimeout(closetimer);
      closetimer = null;
   }
}

// close layer when click-out
document.onclick = mclose; 
var commandNames = ['backspace','left','up','right','down','return']
var oldCommands = []

function grabKeys() {
if (! oldCommands['Backspace']) {
  var env = {editor: editor};

  commandNames.forEach (function(i) {
    oldCommands[i] = canon.findKeyCommand (env, "editor", 0, i)
  }, this)
  //oldCommands['left'] = canon.findKeyCommand (env, "editor", 0, 'left')
  //oldCommands['up'] = canon.findKeyCommand (env, "editor", 0, 'up')
  //oldCommands['right'] = canon.findKeyCommand (env, "editor", 0, 'right')
  //oldCommands['down'] = canon.findKeyCommand (env, "editor", 0, 'down')
  //oldCommands['return'] = canon.findKeyCommand (env, "editor", 0, 'return')
}

canon.addCommand({
    name: '27',
    bindKey: { win: 'Esc', mac: 'Esc', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(27)
    }
})
canon.addCommand({
    name: '37',
    bindKey: { win: 'Left', mac: 'Left', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(37)
    }
})
canon.addCommand({
    name: '38',
    bindKey: { win: 'Up', mac: 'Up', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(38)
    }
})
canon.addCommand({
    name: '39',
    bindKey: { win: 'Right', mac: 'Right', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(39)
    }
})
canon.addCommand({
    name: '8',
    bindKey: { win: 'Backspace', mac: 'Backspace', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(8)
    }
})
canon.addCommand({
    name: '40',
    bindKey: { win: 'Down', mac: 'Down', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(40)
    }
})
canon.addCommand({
    name: '13',
    bindKey: { win: 'Return', mac: 'Return', sender: 'editor' },
    exec: function(env, args, request) {
            autoCompleteGrabkeys(13)
    }
})

}

function ungrabKeys () {
canon.removeCommand ('27')
canon.removeCommand ('37')
canon.removeCommand ('38')
canon.removeCommand ('39')
canon.removeCommand ('40')
canon.removeCommand ('8')
canon.removeCommand ('13')

commandNames.forEach(function(i) {
  if (oldCommands[i]) canon.addCommand (oldCommands[i])
    }, this);

}

//------------------------- inspired from ... TODO get url

function showMenu (currLine, line, column) {
  grabKeys ()
   
  buildMenu (currLine, line, column)

  // position it   
  var x=0
  var y=0
  var x = (column - line.length) *  9.25 + 45;
//  x -= editor.frame.contentDocument.body.scrollLeft;
  x -= editor.container.offsetLeft;
  var y = currLine * 16 + 8;
  y -= editor.container.offsetTop;
  y -= editor.renderer.scrollTop;
  x +=  editor.container.offsetParent.offsetLeft
  y += editor.container.offsetParent.offsetTop

  ddmenu.style.left = x + "px";
  ddmenu.style.top = y + "px";
   
  mopen();
}
 
function keyEventFilter(keycode){   
   if (keycode === 27) return true; // ESC
   if (keycode === 37) return true; // LEFT_ARROW
   if (keycode === 38) return true; // UP_ARROW
   if (keycode === 39) return true; // RIGHT_ARROW
   if (keycode === 40) return true; // DOWN_ARROW
   if (keycode === 8) return true; // BACK SPACE
   if (keycode === 13) return true; // ENTER
   return false;
}
 
function autoCompleteGrabkeys(e_keyCode){
   if (e_keyCode === 27 || e_keyCode === 37 ||  e_keyCode === 39 || e_keyCode === 8) {
    // ESC || LEFT_ARROW || RIGHT_LEFT_ARROW || BACK SPACE 
      mclose(); 
   }
   else if (e_keyCode === 40) // DOWN_ARROW
   {  
      if (currSelected && currSelected.nextSibling) {
         currSelected.className = "a";
         currSelected = currSelected.nextSibling;
         currSelected.className = "razsel";
      }
   }
   else if (e_keyCode === 38) // UP_ARROW
   { 
      if (currSelected && currSelected.previousSibling){
         currSelected.className = "a";
         currSelected = currSelected.previousSibling;
         currSelected.className = "razsel";   
      }
   }
   else if (e_keyCode === 13) // ENTER
   {
      try {
      if (currSelected) currSelected.doitman()
      } catch (e) {}
      mclose(); 
   }
}

//});

//------------------------ quote

function scripsterJump (url,lang, ok, k, ak) {
  var scr = editor.getSession().doc.getTextRange(editor.getSelectionRange());

  if (scr == null || scr.length == 0) scr = editor.getSession().getValue();

  showStatus ("jumping with selection...");
   try {
     window.location = url+'&lang='+lang+'&api_key='+ak+'&k='+k+'&ok='+ok+'&css='+razieCss+'&script='+urlescape(scr)
   } catch(e) {}
}
