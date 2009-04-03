jQuery(function(){
   var stack = [];

   $("span")
      .mouseover(function(e){
         e.stopPropagation();
         // activate current
         var targ = $(e.target).addClass("with-marg-name");
         $("<em>"+ 
              targ.attr("class").replace(/^.*tei-(\S+).*$/,"$1") +"</em>")
            .css({position:"absolute",top:targ.offset().top})
            .appendTo("#margin")
            .addClass("marg-name");
         stack.push(targ);
      })
      .mouseout(function(e){
         e.stopPropagation();
         // clear all
         $("#margin .marg-name").remove();
         stack.length && stack.pop().removeClass("with-marg-name");
      });
});
