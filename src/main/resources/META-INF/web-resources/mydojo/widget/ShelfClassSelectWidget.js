// mydojo/widget/ShelfClassSelectWidget.js
define([
    "dojo/_base/declare",
    "dojo/dom-construct",
    "dojo/dom-style",
    "dojo/_base/lang",
    "dojo/request",
    "dojo/mouse",
    "dojo/query",
    "dojo/on",
	"dojo/fx",
	"dojo/store/Memory",
	"dojo/json",
	"dojo/dom",
	"dojo/_base/array",
	"mydojo/widget/ShelfClassWidget",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
    "dojo/text!./templates/ShelfClassSelectWidget.html"
], function(declare, domConstruct,style,lang,request,mouse,query,on,fx, Memory,JSON,dom,
		arrayUtil,ShelfClassWidget, _WidgetBase, _TemplatedMixin, template){
    return declare([_WidgetBase, _TemplatedMixin], {
        // Our template - important!
        templateString: template,

    	changebuttontext: "Change",

        // Using require.toUrl, we can get a path to our AuthorWidget's space
        // and we want to have a default avatar, just in case
        imagedefault: require.toUrl("/biblio/resources/mydojo/widget/images/blank.png"),

        //passed json text
        jsontext: "",

		displayId: 2345,

        // name of updatenode
        updatenodename:"shelfchange",

        // memorystore:
        memorystore:new dojo.store.Memory(),

        // A class to be applied to the root node in our template
        baseclass: "shelfClassSelectWidget",

        listid:"shelfClassSelectListId",

        postCreate: function(){
		    // Get a DOM node reference for the root of our widget
		    var domNode = this.domNode;


		    // Run any parent postCreate processes - can be done at any point
		    this.inherited(arguments);

		     // fill the memory store
			 var data = JSON.parse(this.jsontext);
			 this.memorystore = new dojo.store.Memory({data: data, idProperty:"id"});

			// fill in current display
			 this._setDisplayIdAttr(this.displayId);
			 
		   // change the name of the button
		     this.showListNode.innerText=this.changebuttontext;
		     // don't want to see the list filling up
		     style.set(this.selectlist,"display","none");

			 // fill the select list
			 arrayUtil.forEach(data,function(shelfdata){
				 var divnode = domConstruct.create("div", {
			            className: "shelfClassWidget",
			            id:shelfdata.id
			      }, this.selectlist);
			      on(divnode,"click",lang.hitch(this,"_setDisplayIdAttr",shelfdata.id));

				 if (shelfdata.imagedisplay) {
					 domConstruct.create("img", {
				            src:shelfdata.imagedisplay
				        },divnode);
				 }
				 domConstruct.create("span", {
			            innerHTML: shelfdata.description
			        }, divnode);
			 }, this);


//var nodeid = event.currentTarget.id;
//_setDisplayIdAttr(nodeid);

		    // Set up our mouseenter/leave events
		    // Using dijit/Destroyable's "own" method ensures that event handlers are unregistered when the widget is destroyed
		    // Using dojo/mouse normalizes the non-standard mouseenter/leave events across browsers
		    // Passing a third parameter to lang.hitch allows us to specify not only the context,
		    // but also the first parameter passed to _changeBackground
		    this.own(
		    		 //query("#shelfClassSelectListId").on("click",lang.hitch(this,"_setDisplayFromEvent",event)),
				     on(this.selectlist, mouse.leave, lang.hitch(this, "_hideSelect")),
				     on(this.showListNode, "click", lang.hitch(this, "_showSelect"))
				     );
		        /*on(domNode, mouse.enter, lang.hitch(this, "_changeBackground", this.mouseBackgroundColor)),
		        on(domNode, mouse.leave, lang.hitch(this, "_changeBackground", this.baseBackgroundColor))*/
		},

		_setDisplayIdAttr: function(id) {
			// alert("here we are + " + id);
			this.displayId = id;

			var updatenode = dojo.byId(this.updatenodename);
			// get object
			var object = this.memorystore.get(id);

			if (object != null) {

				// alert(object.imagedisplay);
				// set image
				var hasimage = false;
				if (object.imagedisplay) {
					hasimage = true;
					this.currentImageNode.src = object.imagedisplay;
				} else {
					// MM fill in!!imagenode.src = "/biblio
				}

				// set shelfinfo
				this.currentInfoNode.innerText = object.textdisplay + " : "
						+ object.description;

				// set shelftext
				this.currentTextNode.innerText = object.textdisplay;

				// set input
				updatenode.value = object.key;
			}
			// hide select
			this._hideSelect();			
			
		},


		_hideSelect: function() {
			fx.wipeOut({ node: this.selectlist }).play();
		},

		_showSelect: function() {
			fx.wipeIn({ node: this.selectlist }).play();
		}


     });


});