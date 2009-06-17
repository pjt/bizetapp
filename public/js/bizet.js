jQuery(function(){
   jQuery.fn.elem = function(){
      var elem = this.attr("class").match(/tei-(\S+)/),
          atts = this.attr("class").match(/teiatt-(\S+)/g),
          atts = !atts ? "" :
                  "@" + atts.map(function(val,i){ 
                                    return val.replace(/^teiatt-/,'').replace(/__/g,' '); })
                            .join("@");
      return elem ? elem[1] + atts : false;
   }

   var review = (function(){
      var stack = [];

      return {
         mouseover: function(e){
            e.stopPropagation();
            var targ = $(e.target),
                elem = targ.elem(),
                targY = targ.offset().top,
                mouseY = e.pageY;
            if (elem) {
               // if xml, activate current
               $("<em>"+ elem +"</em>")
                  .css({position:"absolute",top: Math.abs(targY - mouseY) > 100 ? 
                                                   mouseY - 15 : targY})
                  .appendTo("#margin")
                  .addClass("marg-name");
               stack.push(targ.addClass("with-marg-name"));
            }
         },
         mouseout: function(e){
            e.stopPropagation();
            // clear all
            $("#margin .marg-name").remove();
            stack.length && stack.pop().removeClass("with-marg-name");
         }
      }
   })();

   var reviewing = false;
   $(document).bind("review", function(){
         reviewing = true;
         $("span").mouseover(review.mouseover)
                  .mouseout(review.mouseout);
      })
      .bind("noreview", function(){
            reviewing = false;
            $("span").unbind("mouseover",review.mouseover)
                     .unbind("mouseout",review.mouseout);
      });

   // can activate review with url?review, 
   if (window.location.search.search(/review/) > -1) {
      $(document).trigger("review");
   }
   // or double-click-activated dialog
   $(document).dblclick(function(e){
         window.getSelection().removeAllRanges();
         $.mousewindow(e, 
            $("<a>" + (reviewing ? "Stop Review" : "Review") + "</a>")
               .click(function(){ 
                        $(document).trigger(reviewing ? "noreview" : "review"); 
                        this.parentNode.removeChild(this);
                     }));
      });
});

jQuery.extend({
   mousewindow: (function(){
                  var mw = [],
                      killmw = function() { mw.length && mw.pop().remove(); },
                      clear = function() { killmw(); $(document).unbind("click",clear); };
                  return function(e,arg) {
                     killmw();
                     var d = $("<div/>").css({position: "absolute", 
                                              top: Math.max(5,e.pageY - 20),
                                              //top: e.pageY,
                                              left: e.clientX + 10 })
                                       .addClass("mousewindow")
                                       .append(arg)
                                       .appendTo(document.body);
                     mw.push(d);
                     $(document).click(clear);
                  }
               })()
});

                  
