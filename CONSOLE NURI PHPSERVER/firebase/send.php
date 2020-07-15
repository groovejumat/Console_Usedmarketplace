<?php 
	
	function send_notification ($tokens, $message)
	{
		$url = 'https://fcm.googleapis.com/fcm/send';
		$fields = array(
			 'registration_ids' => $tokens,
			 'data' => $message
			);
        $key = "AAAAMCqv288:APA91bEvof7Zf726CkAeXdr2uS-xStSTHufL09ypVuvZIJSR5w64e6mMx8z4rHOA7WxrOD5cfhMkMvdB4x6L9fMd4d8p6utUDB4oJyp-TSysVOWkcXvMNNN1NXN0h4qCHC6uLfQkVRV0";
		$headers = array(
			'Authorization:key =' . $key,
			'Content-Type: application/json'
			);
       
	 $ch = curl_init();
       curl_setopt($ch, CURLOPT_URL, $url);
       curl_setopt($ch, CURLOPT_POST, true);
       curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
       curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
       curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);  
       curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
       curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
       $result = curl_exec($ch);           
       if ($result === FALSE) {
           die('Curl failed: ' . curl_error($ch));
       }
       curl_close($ch);
       return $result;
	}
	
    
	


     $tokens = array();
     $tokens[0] = "f5D-P4BBv9Y:APA91bE8sPZFULre2B-xQ7_gvNrSfkJ91-7gyMQPD-RWrBfE6x9nrXqyo6m6gPUt3B8oigfoFubyiTltgQunaeYW8vUj7PWTMMctN_fhfJj1sOnmYN3v03SRoQ3u6dWw0h_bQQp__YSL";
  
	
     $myMessage = "Message Test"; 
     if ($myMessage == ""){
		$myMessage = "Newly registered.";
     }

     $message = array("message" => $myMessage);
     $message_status = send_notification($tokens, $message);
     echo $message_status;
	

 ?>

