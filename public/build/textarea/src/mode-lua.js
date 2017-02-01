__ace_shadowed__.define("ace/mode/lua",["require","exports","module","pilot/oop","ace/mode/text","ace/tokenizer","ace/mode/lua_highlight_rules","ace/range"],function(a,b,c){var d=a("pilot/oop"),e=a("ace/mode/text").Mode,f=a("ace/tokenizer").Tokenizer,g=a("ace/mode/lua_highlight_rules").LuaHighlightRules,h=a("ace/range").Range,i=function(){this.$tokenizer=new f((new g).getRules())};d.inherits(i,e),function(){this.getNextLineIndent=function(a,b,c){var d=this.$getIndent(b),e=this.$tokenizer.getLineTokens(b,a),f=e.tokens,g=e.state,h=["function","then","do","repeat"];if(a=="start"){var i=b.match(/^.*[\{\(\[]\s*$/);if(i)d+=c;else for(var j in f){var k=f[j];if(k.type!="keyword")continue;var l=h.indexOf(k.value);if(l!=-1){d+=c;break}}}return d}}.call(i.prototype),b.Mode=i}),__ace_shadowed__.define("ace/mode/lua_highlight_rules",["require","exports","module","pilot/oop","pilot/lang","ace/mode/text_highlight_rules"],function(a,b,c){var d=a("pilot/oop"),e=a("pilot/lang"),f=a("ace/mode/text_highlight_rules").TextHighlightRules,g=function(){var a=e.arrayToMap("break|do|else|elseif|end|for|function|if|in|local|repeat|return|then|until|while|or|and|not".split("|")),b=e.arrayToMap("true|false|nil|_G|_VERSION".split("|")),c=e.arrayToMap("string|xpcall|package|tostring|print|os|unpack|require|getfenv|setmetatable|next|assert|tonumber|io|rawequal|collectgarbage|getmetatable|module|rawset|math|debug|pcall|table|newproxy|type|coroutine|_G|select|gcinfo|pairs|rawget|loadstring|ipairs|_VERSION|dofile|setfenv|load|error|loadfile|sub|upper|len|gfind|rep|find|match|char|dump|gmatch|reverse|byte|format|gsub|lower|preload|loadlib|loaded|loaders|cpath|config|path|seeall|exit|setlocale|date|getenv|difftime|remove|time|clock|tmpname|rename|execute|lines|write|close|flush|open|output|type|read|stderr|stdin|input|stdout|popen|tmpfile|log|max|acos|huge|ldexp|pi|cos|tanh|pow|deg|tan|cosh|sinh|random|randomseed|frexp|ceil|floor|rad|abs|sqrt|modf|asin|min|mod|fmod|log10|atan2|exp|sin|atan|getupvalue|debug|sethook|getmetatable|gethook|setmetatable|setlocal|traceback|setfenv|getinfo|setupvalue|getlocal|getregistry|getfenv|setn|insert|getn|foreachi|maxn|foreach|concat|sort|remove|resume|yield|status|wrap|create|running".split("|")),d=e.arrayToMap("string|package|os|io|math|debug|table|coroutine".split("|")),f=e.arrayToMap("__add|__sub|__mod|__unm|__concat|__lt|__index|__call|__gc|__metatable|__mul|__div|__pow|__len|__eq|__le|__newindex|__tostring|__mode|__tonumber".split("|")),g=e.arrayToMap("".split("|")),h=e.arrayToMap("setn|foreach|foreachi|gcinfo|log10|maxn".split("|")),i="",j="(?:(?:[1-9]\\d*)|(?:0))",k="(?:0[xX][\\dA-Fa-f]+)",l="(?:"+j+"|"+k+")",m="(?:\\.\\d+)",n="(?:\\d+)",o="(?:(?:"+n+"?"+m+")|(?:"+n+"\\.))",p="(?:"+o+")",q=[];this.$rules={start:[{token:"comment",regex:i+"\\-\\-\\[\\[.*\\]\\]"},{token:"comment",regex:i+"\\-\\-\\[\\=\\[.*\\]\\=\\]"},{token:"comment",regex:i+"\\-\\-\\[\\={2}\\[.*\\]\\={2}\\]"},{token:"comment",regex:i+"\\-\\-\\[\\={3}\\[.*\\]\\={3}\\]"},{token:"comment",regex:i+"\\-\\-\\[\\={4}\\[.*\\]\\={4}\\]"},{token:"comment",regex:i+"\\-\\-\\[\\={5}\\=*\\[.*\\]\\={5}\\=*\\]"},{token:"comment",regex:i+"\\-\\-\\[\\[.*$",merge:!0,next:"qcomment"},{token:"comment",regex:i+"\\-\\-\\[\\=\\[.*$",merge:!0,next:"qcomment1"},{token:"comment",regex:i+"\\-\\-\\[\\={2}\\[.*$",merge:!0,next:"qcomment2"},{token:"comment",regex:i+"\\-\\-\\[\\={3}\\[.*$",merge:!0,next:"qcomment3"},{token:"comment",regex:i+"\\-\\-\\[\\={4}\\[.*$",merge:!0,next:"qcomment4"},{token:function(a){var b=/\-\-\[(\=+)\[/,c;return(c=b.exec(a))!=null&&(c=c[1])!=undefined&&q.push(c.length),"comment"},regex:i+"\\-\\-\\[\\={5}\\=*\\[.*$",merge:!0,next:"qcomment5"},{token:"comment",regex:"\\-\\-.*$"},{token:"string",regex:i+"\\[\\[.*\\]\\]"},{token:"string",regex:i+"\\[\\=\\[.*\\]\\=\\]"},{token:"string",regex:i+"\\[\\={2}\\[.*\\]\\={2}\\]"},{token:"string",regex:i+"\\[\\={3}\\[.*\\]\\={3}\\]"},{token:"string",regex:i+"\\[\\={4}\\[.*\\]\\={4}\\]"},{token:"string",regex:i+"\\[\\={5}\\=*\\[.*\\]\\={5}\\=*\\]"},{token:"string",regex:i+"\\[\\[.*$",merge:!0,next:"qstring"},{token:"string",regex:i+"\\[\\=\\[.*$",merge:!0,next:"qstring1"},{token:"string",regex:i+"\\[\\={2}\\[.*$",merge:!0,next:"qstring2"},{token:"string",regex:i+"\\[\\={3}\\[.*$",merge:!0,next:"qstring3"},{token:"string",regex:i+"\\[\\={4}\\[.*$",merge:!0,next:"qstring4"},{token:function(a){var b=/\[(\=+)\[/,c;return(c=b.exec(a))!=null&&(c=c[1])!=undefined&&q.push(c.length),"string"},regex:i+"\\[\\={5}\\=*\\[.*$",merge:!0,next:"qstring5"},{token:"string",regex:i+'"(?:[^\\\\]|\\\\.)*?"'},{token:"string",regex:i+"'(?:[^\\\\]|\\\\.)*?'"},{token:"constant.numeric",regex:p},{token:"constant.numeric",regex:l+"\\b"},{token:function(e){return a.hasOwnProperty(e)?"keyword":b.hasOwnProperty(e)?"constant.language":g.hasOwnProperty(e)?"invalid.illegal":d.hasOwnProperty(e)?"constant.library":h.hasOwnProperty(e)?"invalid.deprecated":c.hasOwnProperty(e)?"support.function":f.hasOwnProperty(e)?"support.function":"identifier"},regex:"[a-zA-Z_$][a-zA-Z0-9_$]*\\b"},{token:"keyword.operator",regex:"\\+|\\-|\\*|\\/|%|\\#|\\^|~|<|>|<=|=>|==|~=|=|\\:|\\.\\.\\.|\\.\\."},{token:"lparen",regex:"[\\[\\(\\{]"},{token:"rparen",regex:"[\\]\\)\\}]"},{token:"text",regex:"\\s+"}],qcomment:[{token:"comment",regex:"(?:[^\\\\]|\\\\.)*?\\]\\]",next:"start"},{token:"comment",merge:!0,regex:".+"}],qcomment1:[{token:"comment",regex:"(?:[^\\\\]|\\\\.)*?\\]\\=\\]",next:"start"},{token:"comment",merge:!0,regex:".+"}],qcomment2:[{token:"comment",regex:"(?:[^\\\\]|\\\\.)*?\\]\\={2}\\]",next:"start"},{token:"comment",merge:!0,regex:".+"}],qcomment3:[{token:"comment",regex:"(?:[^\\\\]|\\\\.)*?\\]\\={3}\\]",next:"start"},{token:"comment",merge:!0,regex:".+"}],qcomment4:[{token:"comment",regex:"(?:[^\\\\]|\\\\.)*?\\]\\={4}\\]",next:"start"},{token:"comment",merge:!0,regex:".+"}],qcomment5:[{token:function(a){var b=/\](\=+)\]/,c=this.rules.qcomment5[0],d;c.next="start";if((d=b.exec(a))!=null&&(d=d[1])!=undefined){var e=d.length,f;(f=q.pop())!=e&&(q.push(f),c.next="qcomment5")}return"comment"},regex:"(?:[^\\\\]|\\\\.)*?\\]\\={5}\\=*\\]",next:"start"},{token:"comment",merge:!0,regex:".+"}],qstring:[{token:"string",regex:"(?:[^\\\\]|\\\\.)*?\\]\\]",next:"start"},{token:"string",merge:!0,regex:".+"}],qstring1:[{token:"string",regex:"(?:[^\\\\]|\\\\.)*?\\]\\=\\]",next:"start"},{token:"string",merge:!0,regex:".+"}],qstring2:[{token:"string",regex:"(?:[^\\\\]|\\\\.)*?\\]\\={2}\\]",next:"start"},{token:"string",merge:!0,regex:".+"}],qstring3:[{token:"string",regex:"(?:[^\\\\]|\\\\.)*?\\]\\={3}\\]",next:"start"},{token:"string",merge:!0,regex:".+"}],qstring4:[{token:"string",regex:"(?:[^\\\\]|\\\\.)*?\\]\\={4}\\]",next:"start"},{token:"string",merge:!0,regex:".+"}],qstring5:[{token:function(a){var b=/\](\=+)\]/,c=this.rules.qstring5[0],d;c.next="start";if((d=b.exec(a))!=null&&(d=d[1])!=undefined){var e=d.length,f;(f=q.pop())!=e&&(q.push(f),c.next="qstring5")}return"string"},regex:"(?:[^\\\\]|\\\\.)*?\\]\\={5}\\=*\\]",next:"start"},{token:"string",merge:!0,regex:".+"}]}};d.inherits(g,f),b.LuaHighlightRules=g})