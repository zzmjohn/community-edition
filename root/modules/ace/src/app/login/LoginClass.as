// Login Action Script 
package app.login
{
	import mx.controls.Alert;
	import flash.events.Event;
	import flash.utils.describeType;
	import mx.core.ClassFactory;
	import mx.containers.Canvas;
	import mx.controls.Text;
	import mx.controls.TextInput;
	import mx.states.State;
	import mx.rpc.mxml.IMXMLSupport;
	import mx.containers.VBox;
	import org.alfresco.framework.service.error.ErrorRaisedEvent;
	import org.alfresco.framework.service.error.ErrorService;
	import org.alfresco.framework.service.authentication.AuthenticationService;
	import org.alfresco.framework.service.authentication.InvalidCredentialsError;

    public class LoginClass extends VBox
	{
		/** UI controls */
		public var username:TextInput;
		public var password:TextInput;
		public var errorMessage:Text;
		
		/** 
		 * Constructor
		 */
		public function LoginClass()
		{
			// Call the super class
			super();
			
			// Register interest with the error service
			ErrorService.instance.addEventListener(ErrorRaisedEvent.ERROR_RAISED, onErrorRaised);	
		}
	
		/**
		 * Event handler for login button link.  
		 * 
		 * Makes call to authenitcation service and redirects user to search dialog.
		 */
		public function onLoginButtonLinkClick():void
		{	
			// Call authentication service to log user in
			AuthenticationService.instance.login(username.text, password.text);
		}
		
		/**
		 * On error raised event handler
		 */
		public function onErrorRaised(event:ErrorRaisedEvent):void
		{
			if (event.errorType == InvalidCredentialsError.INVALID_CREDENTIALS)
			{
				// Switch to the alternative state and set the error message
				currentState = "InvalidCredentials";
				errorMessage.text = event.error.message;
				
			}
		}
	}	
}