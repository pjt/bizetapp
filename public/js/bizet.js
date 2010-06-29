jQuery(function(){
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
   // or mousewindow dialog
   var review_mw = function(){
      return $("<a>" + (reviewing ? "Stop Review" : "Review") + "</a>")
               .click(function(){ 
                        $(document).trigger(reviewing ? "noreview" : "review"); 
               })
   }

   // abbrev checking
   var abbrev_mw = function(){
      return $("<a>Check Abbrevs</a>").click(function(){
               $("span.tei-abbr").each(function(){
                  var $t = $(this);
                  $.getJSON("/bizet/abbrevs/lookup/", {"q": $t.text()},
                        function(data){
                           if (data.length > 0){
                              $t.after(" <b>"+ data.join(" | ") +"</b>");
                           } else {
                              $t.css({"background-color": "red", "color": "white"});
                           }
                        });
                  })
               })
   }


   // section toggling
   /*
   $("span.tei-text span.tei-div > span.tei-head").click(function(e){                     
         e.preventDefault();                                          
         $(this).toggleClass("expanded")
            .parent().children().not(this).slideToggle("slow");
      });
      */
   
   // start w/ divs hidden
   //$("span.tei-body span.tei-div").children().not(".tei-div > span.tei-head").hide();
   // shortcut for expanding all divs
   var expandall_mw = function(){
      return $("<a>Expand Divs</a>").click(function(){
            $("span.tei-body span.tei-div").children().show();
         });
   }

   // grid toggling
   var grid_mw = function(){
      return $("<a>Toggle Grid</a>").click(function(){
            $("#container").toggleClass("showgrid");
         });
   }

   // editing
   var edit_mw = function(){
      return $("<a>Edit Document</a>").click(function(){
            window.location.href = window.location.href.replace(/\/works\//,"/edit/");
         });
   }

   // set up mousewindow HUD
   $(document).dblclick(function(e){
         window.getSelection().removeAllRanges();
         $.mousewindow(e, review_mw, edit_mw, abbrev_mw, expandall_mw, grid_mw);
      });

   // Add <img/> for @facs
   $(".tei-titlePage").addFacsImg();
   // Add <img/> for <graphic>
   $(".tei-graphic").addGraphicImg();

});

jQuery.fn.elem = function(){
   var elem = this.attr("class").match(/tei-(\S+)/),
       atts = this.attr("class").match(/teiatt-(\S+)/g),
       atts = !atts ? "" :
               "@" + atts.map(function(val,i){ 
                                 return val.replace(/^teiatt-/,'').replace(/__/g,' '); })
                         .join("@");
   return elem ? elem[1] + atts : false;
}

jQuery.fn.toggler = function(target){
   return this.each(function(){
         var $t = $(this),
             showhide = /Show/.test($t.text()) ? 
                           {"Show":"Hide", "Hide":"Show"} :
                              {"show":"hide", "hide":"show"};
         $t
            .addClass("toggler")
            .click(function(){
               var $t = $(this),
                   matched = $t.html().match(/show|hide/i),
                   found = matched ? matched[0] : matched;
               target.toggle();
               $t.toggleClass("open");
               if (found){
                  $t.html( $t.html().replace(found, showhide[found]) );
               }
            })
         if (!target.is(":hidden")){
            $t.addClass("open");
         }
      });
}

var imgurl = "http://hdwdev.artsci.wustl.edu/bizet-static/";
jQuery.fn.addFacsImg = function(){
   return this.each(function(){
         var $t = $(this),
             facs = $t.attr("class").match(/teiatt-facs=(\S+)/),
             val = facs ? facs[1] : facs;
         if (val){
            $t.before("<img src='"+ imgurl + val +"'/>");
            $t.hide()
            $("<span>Show transcript</span>")
               .toggler($t)
               .insertBefore($t);
         }
      });
}
jQuery.fn.addGraphicImg = function(){
   return this.each(function(){
         var $t = $(this),
             uri = $t.attr("class").match(/teiatt-uri=(\S+)/),
             val = uri ? uri[1] : uri;
         if (val){
            $t.append("<img src='"+ imgurl + val +"'/>");
         }
      });
}

function argslice(args) { 
   // Slices arguments pseudo-array at index (or indices) given after args
   return Array.prototype.slice.apply(args, 
         Array.prototype.slice.call(arguments, 1));
}

jQuery.extend({
   mousewindow: (function(){
                  var mw = [],
                      killmw = function() { mw.length && mw.pop().remove(); },
                      clear = function() { killmw(); $(document).unbind("click",clear); };
                  return function(e) {
                     killmw();
                     var d = $("<div/>").css({position: "absolute", 
                                              top: Math.max(5,e.pageY - 20),
                                              //top: e.pageY,
                                              left: e.clientX + 10 })
                                       .addClass("mousewindow")
                                       .appendTo(document.body);
                     // append results of calling each argument after 
                     // the first (e, the event object)
                     $.each(argslice(arguments,1),
                        function() { d.append(this()); }
                     );
                     mw.push(d);
                     $(document).click(clear);
                  }
               })()
});

                  
