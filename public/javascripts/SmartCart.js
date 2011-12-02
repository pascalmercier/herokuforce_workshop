/* 
 * SmartCart - jQuery cart plugin 
 * Javascript File 
 * 
 * Home Pages:
 * http://fivelist.summerhost.info
 * http://tech-laboratory.blogspot.com
 * http://techlaboratory.wordpress.com
 *  
 * Date: 05-SEP-2009
 * Version: 0.95 beta
 */
(function($) {

	$.fn.smartCart = function(options) {

		var defaults = {
    			// Most Required Options - Element Selectors 
    			itemSelector: '.scItemButton', // collection of buttons which add items to cart 
          cartListSelector: '#sc_cartlist', // element in which selected items are listed
    			subTotalSelector: '#sc_subtotal', // element in which subtotal shows  
          messageBox: '#sc_message', // element in which messages are displayed	
          // Prefix Item Attribute Selector - Required
          itemNameSelectorPrefix: '#prod_name', // combination of this data and product/item id is used to get an element in product list with the item name (can be a div/span)
          itemQuantitySelectorPrefix: '#prod_qty', // for quantity ( should be a textbox/<select>)
          itemPriceSelectorPrefix: '#prod_price',  // for price (can be a div/span)
          // Text Labels
    			removeLabel: 'remove',		// text for the "remove" link
    			addedLabel: '1 item added to cart',	// text to show message when an item is added
    			removedLabel: '1 item removed from cart',	// text to show message when an item is removed
    			emptyCartLabel: '<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Your Cart is Empty!</b> You can select items from the product list.',	// text or empty cart (can be even html)
          // Css Classes
    			selectClass: 'scProductSelect',	// css class for the <select> element
    			listClass: 'scULList',					// css class for the list ($ol)
    			listItemClass: 'scCartListItem', // css class for the <li> list items
    			listItemLabelClass: 'scListItemLabel',	// css class for the label text that in list items
    			removeClass: 'scListItemRemove',			// css class given to the "remove" link
    			highlightClass: 'scHighlight',				// css class given to the highlight <span>   
          // Other Options     	
    			highlight: true					// toggle highlight effect to the added item
			};

		var options = $.extend(defaults,options);

		return this.each(function(index) {  
      var scCartCont = $(this);  // a container that is wrapped around the cart
			var scItemList = $("select",scCartCont);	// the hidden select element holds the selected product list
      var scItemAnchors = $(options.itemSelector); // "add to cart" buttons
      var scCartList = $(options.cartListSelector,scCartCont); //Cart list elemetn
      var scSubtotalDisp = $(options.subTotalSelector,scCartCont); //Subtotal display element
      var scMessageBox = $(options.messageBox); //Message display element
      var lastAddedItemId = 0;
			var $ol; 	// the list that we are manipulating

			function init() {
          $ol = $("<ul></ul>").addClass(options.listClass).attr('id', options.listClass);
				  resetCartData();
				  // Add Change Event
				  scItemList.change(selectChangeEvent).addClass(options.selectClass);
          // "Add to cart" button event
				  scItemAnchors.click(addCartItemEvent); 
			}
			
			function addCartItemEvent(e) {
          var prodId   = lastAddedItemId = $(this).attr('rel');
  				var prodName = $(options.itemNameSelectorPrefix+prodId).html();          
  				var prodQty  = $(options.itemQuantitySelectorPrefix+prodId).val();
  				if(prodQty == ''){
  				  prodQty = 1;
  				}
  				var $option = $("<option></option>").text(prodName).val(prodId+"|"+prodQty).attr("selected", true); 
  				$(scItemList).append($option).change();
  				$(options.itemQuantitySelectorPrefix+prodId).val('1');         
  				return false;
      }
			
      function resetCartData() {
				scItemList.children("option").each(function(n) {
					var $t = $(this);
					
	        if(!$t.is(":selected")){ // make every entries in the <select> as selected
            $t.attr('selected', true);
          }
          var tmpVal = $t.val().split('|');          
          if(!$t.attr('rel')){ // set the rel if not
            $t.attr('rel',  tmpVal[0]);     
          }
					$t.attr('id', 'sc' + 'option' + n);
					var id = $t.attr('id');	
					
					if($t.html()==''){ // set the item name if not
					    var itemName =  $(options.itemNameSelectorPrefix+tmpVal[0]).html();
              $t.html(itemName);
          }

          // logic for grouping multiple item entries
          var listRel = $t.attr('rel');
          var listItems = scItemList.children("option[rel=" + $t.attr('rel') + "]"); 
          if(listItems.length>1){
            var mulPId = 0;
            var mulQty = 0;
            var tmpOption = $t;
            listItems.each(function(n) {
                var tmpRel = $(this,scItemList).attr('value');
                var tmpRelVal = tmpRel.split('|');
                mulPId = tmpRelVal[0];
                mulQty = mulQty + (tmpRelVal[1]-0);
            });
            scItemList.children("option[rel=" + $t.attr('rel') + "]").remove();
            $t.val(mulPId+"|"+mulQty)
            scItemList.append($t); 
          }
				}); 
				resetListItem();	
				calcualteSubTotal();
			}
			function resetListItem() {
				// refresh the html list 
				$ol.html('');
				if(scItemList.children("option").length > 0){
            scItemList.children("option").each(function(n) {
                var tmpOpt = $(this);
        				var $removeLink = $("<a></a>")
        					.attr("href", "#")
        					.addClass(options.removeClass)
        					.prepend(options.removeLabel)
        					.click(function() { 
        						dropListItem($(this).parent('li').attr('rel')); 
        						return false; 
        					});
        					
                  var itemVal = $(this,scItemList).val();
                  var tmp = itemVal.split('|');
                  var itemId = tmp[0];
                  var itemQty = tmp[1];
                  var itemPrice =  $(options.itemPriceSelectorPrefix+itemId).html();
                  var itemTotal = itemPrice*itemQty;
                  itemTotal = itemTotal.toFixed(2);
                  var itemText = "<table width='100%'><tr><td>"+tmpOpt.html()+"</td><td width='70px'>"+itemQty+
                                 "</td><td width='130px'>"+itemTotal+"</td></tr></table>";
          				var $itemLabel = $("<span></span>")
          					.addClass(options.listItemLabelClass)
          					.html(itemText); 
          
          				var $item = $("<li></li>")
          					.attr('rel', itemId)
          					.addClass(options.listItemClass)
          					.append($itemLabel)
          					.append($removeLink)
          					.hide();
          
          				$ol.append($item);
          				$(options.cartListSelector,scCartCont).prepend($ol);
          				$item.show();
            });
            $(options.cartListSelector).scrollTo($('li[rel='+lastAddedItemId+']',scCartCont));
            if(options.highlight){
              $('li[rel='+lastAddedItemId+']',scCartCont).effect('highlight', {}, 2000);               
            }
            lastAddedItemId = 0;
        }else{
          // Cart is empty
          $ol.html(options.emptyCartLabel);
        }
        
			}
			
			function selectChangeEvent(e) {
				$ol.empty();
				resetCartData();
				setHighlight(options.addedLabel);
			}

			function dropListItem(relId, highlightItem) { 
				$item = $ol.children("li[rel=" + relId + "]"); 
        $item.remove();
				scItemList.children("option[rel=" + relId + "]").remove();
        scItemList.change();
				setHighlight(options.removedLabel);
			}
			
      function calcualteSubTotal() {
        var subTotal = 0;
				scItemList.children("option").each(function(n) {
					var $t = $(this);
            var tmpVal = $t.val().split('|');
            var itemId = tmpVal[0];
            var itemQty = tmpVal[1];
            var itemPrice =  $(options.itemPriceSelectorPrefix+itemId).html();
            subTotal += itemPrice*itemQty;     
				}); 
				subTotal = subTotal.toFixed(2);
				$(options.subTotalSelector).html(subTotal);
			}

      function setHighlight(label) {
				var $highlight = $(options.messageBox)
					.hide()
					.addClass(options.highlightClass)
					.html(label); 
					 
				$highlight.fadeIn("fast", function() {
					setTimeout(function() { $highlight.fadeOut("slow"); }, 2000); 
				}); 
			}
			
			// Initialize
			init();
		});
	};

})(jQuery); 
