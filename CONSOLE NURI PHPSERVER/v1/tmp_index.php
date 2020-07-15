<?php
//api설계를 하여 이를 통해 http요청 송수신을 할 수 있도록 합니다.
header('Content-Type: text/html; charset=utf-8');

error_reporting(-1);
ini_set('display_errors', 'On');

require_once '../include/db_handler.php';
require '.././libs/Slim/Slim.php';

\Slim\Slim::registerAutoloader();

$app = new \Slim\Slim();

// User login
$app->post('/user/login', function() use ($app) {
    // check for required params
    verifyRequiredParams(array('name', 'email'));

    // reading post params
    $name = $app->request->post('name');
    $email = $app->request->post('email');

    // validating email address
    validateEmail($email);

    $db = new DbHandler();
    $response = $db->createUser($name, $email);

    // echo json response
    echoRespnse(200, $response);
});

/**
 * Delete single productitem
 *  */
$app->get('/deleteproduct/:id', function($product_id) {

    $response = array();
    $db = new DbHandler(); //여기서 getproductfunction을 실행 시킨다//

    $result = $db->deleteProduct($product_id);

    echoRespnse(200, "deleted.");
});


/**
 * 해당 user값을 가진 모든 채팅룸을 패치합니다. 가지고 옵니다.
 *  */
$app->post('/getUserschatrooms', function() {
    global $app; //post방식에서는 이를 기용한다. 이 구문이 무슨 뜻을 하는 것이길래 필요한 것일까?

    $response = array();
    $db = new DbHandler(); //여기서 getproductfunction을 실행 시킨다//

    $user_name = $app->request->post('user_name'); //post로 username 값을 붙여 줍니다. 여기서 $app을 호출해 주어야 합니다.

    $result = $db->getUsersChatrooms($user_name);

    $response["error"] = false;
    $response["chat_rooms"] = array();

    // pushing single chat room into array
    while ($chat_room = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["chat_room_id"] = $chat_room["chat_room_id"];
        $tmp["name"] = $chat_room["name"];
        $tmp["product_id"] = $chat_room["product_id"];
        $tmp["product_name"] = $chat_room["product_name"];
	//echo $chat_room["name"];
        $tmp["created_at"] = $chat_room["created_at"];
        //$tmp=array_map("utf8_encode",$tmp)
        array_push($response["chat_rooms"], $tmp);
    }

    //$response = array_map("utf8_encode",$response);
    echoRespnse(200, $response);
});




/**
 * 해당 user값을 가진 모든 채팅룸을 패치합니다. 가지고 옵니다.
 *  */
$app->post('/getSellerchatrooms', function() {
    global $app; //post방식에서는 이를 기용한다. 이 구문이 무슨 뜻을 하는 것이길래 필요한 것일까?

    $response = array();
    $db = new DbHandler(); //여기서 getproductfunction을 실행 시킨다//

    $seller_name = $app->request->post('seller_name'); //post로 username 값을 붙여 줍니다. 여기서 $app을 호출해 주어야 합니다.

    $result = $db->getSellerChatrooms($seller_name);

    $response["error"] = false;
    $response["chat_rooms"] = array();

    // pushing single chat room into array
    while ($chat_room = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["chat_room_id"] = $chat_room["chat_room_id"];
        $tmp["name"] = $chat_room["name"];
        $tmp["product_id"] = $chat_room["product_id"];
        $tmp["product_name"] = $chat_room["product_name"];
	//echo $chat_room["name"];
        $tmp["created_at"] = $chat_room["created_at"];
        //$tmp=array_map("utf8_encode",$tmp)
        array_push($response["chat_rooms"], $tmp);
    }

    //$response = array_map("utf8_encode",$response);
    echoRespnse(200, $response); //값 반환
});



/* * *
 * Updating user
 *  we use this url to update user's gcm registration id
 */
$app->put('/user/:id', function($user_id) use ($app) {
    global $app;

    verifyRequiredParams(array('gcm_registration_id')); //이게 무슨 뜻인지 파악

    $gcm_registration_id = $app->request->put('gcm_registration_id');

    $db = new DbHandler();
    $response = $db->updateGcmID($user_id, $gcm_registration_id); // 해당 함수를 실행하도록 만듬

    echoRespnse(200, $response);
});

/* * *
 * fetching all chat rooms
 */
$app->get('/chat_rooms', function() {
    $response = array();
    $response = array_map("utf8_encode",$response);
    $db = new DbHandler();

    // fetching all user tasks
    $result = $db->getAllChatrooms();

    $response["error"] = false;
    $response["chat_rooms"] = array();

    // pushing single chat room into array
    while ($chat_room = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["chat_room_id"] = $chat_room["chat_room_id"];
        $tmp["name"] = $chat_room["name"];
	//echo $chat_room["name"];
        $tmp["created_at"] = $chat_room["created_at"];
        //$tmp=array_map("utf8_encode",$tmp)
        array_push($response["chat_rooms"], $tmp);
    }

    //$response = array_map("utf8_encode",$response);
    echoRespnse(200, $response);
});

/* * *
 * fetching all products
 */
$app->get('/products', function() {
    $response = array();
    $db = new DbHandler();

    // fetching all user tasks
    $result = $db->getAllproducts();

    $response["error"] = false;
    $response["products"] = array();

    // 모든 정보값을 가지고 옵니다.
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["product_id"] = $products["product_id"];
        $tmp["productname"] = $products["productname"];
        $tmp["created_at"] = $products["created_at"];
        $tmp["price"] = $products["price"];
        $tmp["description"] = $products["description"];
	$tmp["imagelist"] = $products["imagelist"];
	$tmp["writer"]=$products["writer"];
	$tmp["category"]=$products["category"];
        array_push($response["products"], $tmp);
    }

    echoRespnse(200, $response);
});



/**
 *메시지만드는 api를 이용해서 채팅방을 생성하는 api를 만들어 볼 것이다. post로 값을 넘기도록 한다.
 *username의 이름을 가진, 채팅 룸을 구현한다.
 *  */
$app->post('/createchatroom', function() { //채팅방의 이름은 유저의 닉네임으로 정해진다.
    global $app; //글로벌 변수 app?
    $db = new DbHandler();

    verifyRequiredParams(array('user_name', 'product_id', 'product_name','product_writer')); // 바디값으로 필요한 값을 확인한다.

    $user_name = $app->request->post('user_name');
    $product_id = $app->request->post('product_id');
    $product_name = $app->request->post('product_name');
    $product_writer = $app->request->post('product_writer');


    $response = $db->createChatroom($user_name, $product_id, $product_name, $product_writer);

    if ($response['error'] == false) {
        // require_once __DIR__ . '/../libs/gcm/gcm.php'; //FCM푸쉬를 사용하기 위해서 gcm.php, push.php를 불로온다.
        // require_once __DIR__ . '/../libs/gcm/push.php'; //
        // $gcm = new GCM(); //객체 생성
        // $push = new Push(); //객체 생성.

        // get the user using userid
        // $user = $db->getUser($user_id);

        // $data = array();
        // $data['user'] = $user;
        // $data['message'] = $response['message'];
        // $data['chat_room_id'] = $chat_room_id;

        // //FCM기능 사용 (이부분에서 채팅 방생성시 알람 처리를 해야 할 수 있다.)
        // $push->setTitle("Google Cloud Messaging");
        // $push->setIsBackground(FALSE);
        // $push->setFlag(PUSH_FLAG_CHATROOM); //플레그 값을 세팅 (해당 플레그에 따라서 안드로이드 단에서 어떤식으로 처리를 해야 할지에 대해서 결정한다.)
        // $push->setData($data); //데이터 값을 세팅 (푸쉬로 이를 보내 주는 것)

        // echo json_encode($push->getPush());exit;

        // sending push message to a topic
        // $gcm->sendToTopic('topic_' . $chat_room_id, $push->getPush()); //해당 토픽으로 값을 전달한다.

        // $response['user'] = $user;
        $response['error'] = false;
    }

    echoRespnse(200, $response);
});



/**
 * Messaging in a chat room
 * Will send push notification using Topic Messaging //내가 지금 사용하고 있는 부분이 이부분이다.
 *  */
$app->post('/chat_rooms/:id/message', function($chat_room_id) {
    global $app;
    $db = new DbHandler();

    verifyRequiredParams(array('user_id', 'message'));

    $user_id = $app->request->post('user_id');
    $message = $app->request->post('message');

    $response = $db->addMessage($user_id, $chat_room_id, $message); //addMessage라는 함수를 호출 시킨다.

    //$response의 값이 잘되어졌다면, 새로운 메시지를 insert하는 작업이 잘 되어졌다면, push메시지를 보낸다.

    if ($response['error'] == false) {
        require_once __DIR__ . '/../libs/gcm/gcm.php';
        require_once __DIR__ . '/../libs/gcm/push.php';
        $gcm = new GCM();
        $push = new Push();

        // get the user using userid
        $user = $db->getUser($user_id); //post로 보낸 user_id값을 참조함.

        $data = array();
        $data['user'] = $user;
        $data['message'] = $response['message'];
        $data['chat_room_id'] = $chat_room_id;

        $push->setTitle("Google Cloud Messaging"); //이건 필요한지 잘 모르겠다.
        $push->setIsBackground(FALSE);
        $push->setFlag(PUSH_FLAG_CHATROOM); // 채팅룸의 플래그를 설정(안드로이드에서 onreceive처리시에 이를 활용한다.)
        $push->setData($data); // 데이터 값을 세팅

        // echo json_encode($push->getPush());exit;

        // sending push message to a topic
        $gcm->sendToTopic('topic_' . $chat_room_id, $push->getPush()); //topic에 해당하는 사람들에게맨 메시지를 보냄.

        $response['user'] = $user;
        $response['error'] = false;
    }

    echoRespnse(200, $response);
});


/**
 * Sending push notification to a single user
 * We use user's gcm registration id to send the message
 * * */
$app->post('/users/:id/message', function($to_user_id) {
    global $app;
    $db = new DbHandler();

    verifyRequiredParams(array('message'));

    $from_user_id = $app->request->post('user_id');
    $message = $app->request->post('message');

    $response = $db->addMessage($from_user_id, $to_user_id, $message);

    if ($response['error'] == false) {
        require_once __DIR__ . '/../libs/gcm/gcm.php';
        require_once __DIR__ . '/../libs/gcm/push.php';
        $gcm = new GCM();
        $push = new Push();

        $user = $db->getUser($to_user_id);

        $data = array();
        $data['user'] = $user;
        $data['message'] = $response['message'];
        $data['image'] = '';

        $push->setTitle("Google Cloud Messaging");
        $push->setIsBackground(FALSE);
        $push->setFlag(PUSH_FLAG_USER);
        $push->setData($data);

        // sending push message to single user
        $gcm->send($user['gcm_registration_id'], $push->getPush());

        $response['user'] = $user;
        $response['error'] = false;
    }

    echoRespnse(200, $response);
});


/**
 * Sending push notification to multiple users
 * We use gcm registration ids to send notification message
 * At max you can send message to 1000 recipients
 * * */
$app->post('/users/message', function() use ($app) {

    $response = array();
    verifyRequiredParams(array('user_id', 'to', 'message'));

    require_once __DIR__ . '/../libs/gcm/gcm.php';
    require_once __DIR__ . '/../libs/gcm/push.php';

    $db = new DbHandler();

    $user_id = $app->request->post('user_id');
    $to_user_ids = array_filter(explode(',', $app->request->post('to')));
    $message = $app->request->post('message');

    $user = $db->getUser($user_id);
    $users = $db->getUsers($to_user_ids);

    $registration_ids = array();

    // preparing gcm registration ids array
    foreach ($users as $u) {
        array_push($registration_ids, $u['gcm_registration_id']);
    }

    // insert messages in db
    // send push to multiple users
    $gcm = new GCM();
    $push = new Push();

    // creating tmp message, skipping database insertion
    $msg = array();
    $msg['message'] = $message;
    $msg['message_id'] = '';
    $msg['chat_room_id'] = '';
    $msg['created_at'] = date('Y-m-d G:i:s');

    $data = array();
    $data['user'] = $user;
    $data['message'] = $msg;
    $data['image'] = '';

    $push->setTitle("Google Cloud Messaging");
    $push->setIsBackground(FALSE);
    $push->setFlag(PUSH_FLAG_USER);
    $push->setData($data);

    // sending push message to multiple users
    $gcm->sendMultiple($registration_ids, $push->getPush());

    $response['error'] = false;

    echoRespnse(200, $response);
});

//이부분을 참조해서 리시버에게 보내면 됌.
$app->post('/users/send_to_all', function() use ($app) {

    $response = array();
    verifyRequiredParams(array('user_id', 'message'));

    require_once __DIR__ . '/../libs/gcm/gcm.php';
    require_once __DIR__ . '/../libs/gcm/push.php';

    $db = new DbHandler();

    $user_id = $app->request->post('user_id');
    $message = $app->request->post('message');

    require_once __DIR__ . '/../libs/gcm/gcm.php';
    require_once __DIR__ . '/../libs/gcm/push.php';
    $gcm = new GCM();
    $push = new Push();

    // get the user using userid
    $user = $db->getUser($user_id);

    // creating tmp message, skipping database insertion
    $msg = array();
    $msg['message'] = $message;
    $msg['message_id'] = '';
    $msg['chat_room_id'] = '';
    $msg['created_at'] = date('Y-m-d G:i:s');

    $data = array();
    $data['user'] = $user;
    $data['message'] = $msg;
    $data['image'] = 'https://www.androidhive.info/wp-content/uploads/2016/01/Air-1.png';

    $push->setTitle("Google Cloud Messaging");
    $push->setIsBackground(FALSE);
    $push->setFlag(PUSH_FLAG_USER);
    $push->setData($data);

    // sending message to topic `global`
    // On the device every user should subscribe to `global` topic
    $gcm->sendToTopic('global', $push->getPush());

    $response['user'] = $user;
    $response['error'] = false;

    echoRespnse(200, $response);
});

/**
 * Fetching single chat room including all the chat messages
 *  */
$app->get('/chat_rooms/:id', function($chat_room_id) {
    global $app;
    $db = new DbHandler();

    $result = $db->getChatRoom($chat_room_id);

    $response["error"] = false;
    $response["messages"] = array();
    $response['chat_room'] = array();

    $i = 0;
    // looping through result and preparing tasks array
    while ($chat_room = $result->fetch_assoc()) {
        // adding chat room node
        if ($i == 0) {
            $tmp = array();
            $tmp["chat_room_id"] = $chat_room["chat_room_id"];
            $tmp["name"] = $chat_room["name"];
            $tmp["created_at"] = $chat_room["chat_room_created_at"];
            $response['chat_room'] = $tmp;
        }

        if ($chat_room['user_id'] != NULL) {
            // message node
            $cmt = array();
            $cmt["message"] = $chat_room["message"];
            $cmt["message_id"] = $chat_room["message_id"];
            $cmt["created_at"] = $chat_room["created_at"];

            // user node
            $user = array();
            $user['user_id'] = $chat_room['user_id'];
            $user['username'] = $chat_room['username'];
            $cmt['user'] = $user;

            array_push($response["messages"], $cmt);
        }
    }

    echoRespnse(200, $response);
});

/**
 * Fetching single productitem
 *  */
$app->get('/product/:id', function($product_id) {
    //global $app;
    $response = array();
    $db = new DbHandler(); //여기서 getproductfunction을 실행 시킨다//

    $result = $db->getProduct($product_id);

    $response["error"] = false;
    $response["product"] = array();

    // pushing single chat room into array
    while ($product = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["product_id"] = $product["product_id"];
        $tmp["productname"] = $product["productname"];
	$tmp["price"] = $product["price"];
	$tmp["writer"] = $product["writer"];
	$tmp["created_at"] = $product["created_at"];
	$tmp["description"] = $product["description"];
	$tmp["category"] = $product["category"];
        $tmp["imagelist"] = $product["imagelist"];
        array_push($response["product"], $tmp);
    }

    echoRespnse(200, $response);
});


/**
 * Verifying required params posted or not // 포스트 방식으로써 데이터를 보냈을 때에, 파라미터값을 가지고 오기 위한 방법.
 */
function verifyRequiredParams($required_fields) {
    $error = false;
    $error_fields = "";
    $request_params = array();
    $request_params = $_REQUEST;
    // Handling PUT request params
    if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
        $app = \Slim\Slim::getInstance();
        parse_str($app->request()->getBody(), $request_params); //요청 파라미터 값을 가지고 온다.
    }

    //각각의 필드를 확인하여 이를 대입한다.
    foreach ($required_fields as $field) {
        if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
            $error = true;
            $error_fields .= $field . ', ';
        }
    }

    //요청되어진 필드가 비워졌을 때의 값을 확인한다.
    if ($error) {
        // Required field(s) are missing or empty
        // echo error json and stop the app
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response["error"] = true;
        $response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
        echoRespnse(400, $response);
        $app->stop();
    }
}


//이 부분에 대해서 아직 해석을 하지는 못했다.
/**
 * Validating email address
 */
function validateEmail($email) {
    $app = \Slim\Slim::getInstance(); //슬림에 있는 인스턴스 값을 $app에 담아낸다.
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response["error"] = true;
        $response["message"] = 'Email address is not valid';
        echoRespnse(400, $response);
        $app->stop();
    }
}

function IsNullOrEmptyString($str) {
    return (!isset($str) || trim($str) === '');
}

/**
 * Echoing json response to client
 * @param String $status_code Http response code
 * @param Int $response Json response
 */
function echoRespnse($status_code, $response) {
    $app = \Slim\Slim::getInstance();
    // Http response code
    $app->status($status_code);

    // setting response content type to json
    $app->contentType('application/json');

    echo json_encode($response);
}

$app->run();
?>
