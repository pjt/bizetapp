jQuery(function(){
   jQuery.fn.elem = function(){
      var elem = this.attr("class").match(/tei-(\S+)/);
      return elem ? elem[1] : false;
   }

   var review = (function(){
      var stack = [];

      return {
         mouseover: function(e){
            e.stopPropagation();
            var targ = $(e.target),
                elem = targ.elem();
            if (elem) {
               // if xml, activate current
               $("<em>"+ elem +"</em>")
                  .css({position:"absolute",top:targ.offset().top})
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

   $(document).bind("review", function(){
         $("span").mouseover(review.mouseover)
                  .mouseout(review.mouseout);
      })
      .bind("noreview", function(){
            $("span").unbind("mouseover",review.mouseover)
                     .unbind("mouseout",review.mouseout);
      });

   if (window.location.search.search(/review/) > -1) {
      $(document).trigger("review");
   }
});
