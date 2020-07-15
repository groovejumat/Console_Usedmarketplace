<?php

/**
 * Class to handle all db operations
 * This class will have CRUD methods for database tables
 *
 * @author Ravi Tamada
 */
class DbHandler {

    private $conn;

    function __construct() {
        require_once dirname(__FILE__) . '/db_connect.php';
        // opening db connection
        $db = new DbConnect();
        $this->conn = $db->connect();
    }

    // creating new user if not existed
    public function createUser($name, $email) {
        $response = array();

        // First check if user already existed in db
        if (!$this->isUserExists($email)) {
            // insert query
            $stmt = $this->conn->prepare("INSERT INTO users(name, email) values(?, ?)");
            $stmt->bind_param("ss", $name, $email);

            $result = $stmt->execute();

            $stmt->close();

            // Check for successful insertion
            if ($result) {
                // User successfully inserted
                $response["error"] = false;
                $response["user"] = $this->getUserByEmail($email);
            } else {
                // Failed to create user
                $response["error"] = true;
                $response["message"] = "Oops! An error occurred while registereing";
            }
        } else {
            // User with same email already existed in the db
            $response["error"] = false;
            $response["user"] = $this->getUserByEmail($email);
        }

        return $response;
    }

    // updating user GCM registration ID
    public function updateGcmID($user_id, $gcm_registration_id) {
        $response = array();
        $stmt = $this->conn->prepare("UPDATE users SET gcm_registration_id = ? WHERE user_id = ?");
        $stmt->bind_param("si", $gcm_registration_id, $user_id);

        if ($stmt->execute()) {
            // User successfully updated
            $response["error"] = false;
            $response["message"] = 'GCM registration ID updated successfully';
        } else {
            // Failed to update user
            $response["error"] = true;
            $response["message"] = "Failed to update GCM registration ID";
            $stmt->error;
        }
        $stmt->close();

        return $response;
    }

    // fetching single user by id 유저에 대한 모든 정보를 가지고 온다. user_id값을 기반으로 (고유값)
    public function getUser($user_id) {
        $stmt = $this->conn->prepare("SELECT user_id, name, email, gcm_registration_id, created_at FROM users WHERE user_id = ?");
        $stmt->bind_param("s", $user_id);
        if ($stmt->execute()) {
            // $user = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($user_id, $name, $email, $gcm_registration_id, $created_at);
            $stmt->fetch();
            $user = array();
            $user["user_id"] = $user_id;
            $user["name"] = $name;
            $user["email"] = $email;
            $user["gcm_registration_id"] = $gcm_registration_id;
            $user["created_at"] = $created_at;
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }

    // fetching multiple users by ids
    public function getUsers($user_ids) {

        $users = array();
        if (sizeof($user_ids) > 0) {
            $query = "SELECT user_id, name, email, gcm_registration_id, created_at FROM users WHERE user_id IN (";

            foreach ($user_ids as $user_id) {
                $query .= $user_id . ',';
            }

            $query = substr($query, 0, strlen($query) - 1);
            $query .= ')';

            $stmt = $this->conn->prepare($query);
            $stmt->execute();
            $result = $stmt->get_result();

            while ($user = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["user_id"] = $user['user_id'];
                $tmp["name"] = $user['name'];
                $tmp["email"] = $user['email'];
                $tmp["gcm_registration_id"] = $user['gcm_registration_id'];
                $tmp["created_at"] = $user['created_at'];
                array_push($users, $tmp);
            }
        }

        return $users;
    }

    // messaging in a chat room / to persional message //이 함수를 호출시킨다.
    public function addMessage($user_id, $chat_room_id, $message) {
        $response = array();

        $stmt = $this->conn->prepare("INSERT INTO messages (chat_room_id, user_id, message) values(?, ?, ?)");
        $stmt->bind_param("iis", $chat_room_id, $user_id, $message);

        $result = $stmt->execute();

        if ($result) {
            $response['error'] = false;

            // get the message
            $message_id = $this->conn->insert_id;
            $stmt = $this->conn->prepare("SELECT message_id, user_id, chat_room_id, message, created_at FROM messages WHERE message_id = ?");
            $stmt->bind_param("i", $message_id);
            if ($stmt->execute()) {
                $stmt->bind_result($message_id, $user_id, $chat_room_id, $message, $created_at);
                $stmt->fetch();
                $tmp = array();
                $tmp['message_id'] = $message_id;
                $tmp['chat_room_id'] = $chat_room_id;
                $tmp['message'] = $message;
                $tmp['created_at'] = $created_at;
                $response['message'] = $tmp;
            }
        } else {
            $response['error'] = true;
            $response['message'] = 'Failed send message';
        }

        return $response;
    }



    // messaging in a chat room / to persional message //이 함수를 호출시킨다.
    public function createChatroom($user_name, $product_id, $product_name,$product_writer) {
        $response = array(); //응답 배열 생성.

        //INSERT INTO `chat_rooms`(`name`, `product_id`) VALUES ('user3','16') 다음과 같은 방식으로 쿼리 처리 진행.
        $stmt = $this->conn->prepare("INSERT INTO chat_rooms (name, product_id,product_name,product_writer) values(?, ?, ?,?)"); //쿼리준비
        $stmt->bind_param("siss", $user_name, $product_id, $product_name,$product_writer); //값 연결

        $result = $stmt->execute(); //쿼리문 실행.

        //result값이 true라면....
        if ($result) {
            $response['error'] = false;

            // get the message
            $chat_room_id = $this->conn->insert_id; //해당 구문은 insert되어진 값의 primary key를 가지고 오는 역할을 한다.
            $stmt = $this->conn->prepare("SELECT chat_room_id, name, product_id, product_name, product_writer, created_at FROM chat_rooms WHERE chat_room_id = ?");
            $stmt->bind_param("i", $chat_room_id);
            if ($stmt->execute()) { //해당 쿼리를 실행한 결과 값을 가지고 온다.
                $stmt->bind_result($chat_room_id, $name, $product_id, $product_name, $product_writer, $created_at);
                $stmt->fetch();
                $tmp = array();
                $tmp['chat_room_id'] = $chat_room_id;
                $tmp['name'] = $name;
                $tmp['product_id'] = $product_id;
                $tmp['product_name'] = $product_name;
                $tmp['product_writer'] = $product_writer;
                $tmp['created_at'] = $created_at;
                $response['createdroom'] = $tmp;
            }
        } else {
            $response['error'] = true;
            $response['message'] = 'Failed send message';
        }

        return $response;
    }


    // fetching all chat rooms
    public function getAllChatrooms() {
        $stmt = $this->conn->prepare("SELECT * FROM chat_rooms");
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }


    // 유저가 현재 거래 중인 채팅방을 모두 생성.
    public function getUsersChatrooms($username) {
        $stmt = $this->conn->prepare("SELECT * FROM chat_rooms WHERE name = ?");
        $stmt->bind_param("s",$username); //파라미터 값을 연결.
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }

    //  판매자가 현재 거래 중인 채팅방을 모두 생성.
    public function getSellerChatrooms($productwriter) {
        $stmt = $this->conn->prepare("SELECT * FROM chat_rooms WHERE product_writer = ?");
        $stmt->bind_param("s",$productwriter); //파라미터 값을 연결.
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }


    //현재 모든 작성글들을 가지고 옵니다.
    public function getAllproducts() {
        $stmt = $this->conn->prepare("SELECT * FROM product ORDER BY created_at DESC");
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }



    // get product info
    // 해당 id값을 가진 제품 정보를 가지고옵니다.
    function getProduct($product_id) {
        $stmt = $this->conn->prepare("SELECT * FROM product WHERE product_id = ?");
        $stmt->bind_param("i", $product_id);
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }

    // delete product
    // 해당 id값을 가진 제품을 삭제 하도록 합니다.
    function deleteProduct($product_id){
	$stmt = $this->conn->prepare("DELETE FROM product WHERE product_id = ?");
	$stmt->bind_param("i", $product_id);
	$stmt->execute();
	$tasks = $stmt->get_result();
	$stmt->close();
	return $tasks;
	}


    // fetching single chat room by id //나중에 이부분에 대해서 처리를 해야 될 순간이 올 수도 있다.
    function getChatRoom($chat_room_id) {
        $stmt = $this->conn->prepare("SELECT cr.chat_room_id, cr.name, cr.created_at as chat_room_created_at, u.name as username, c.* FROM chat_rooms cr LEFT JOIN messages c ON c.chat_room_id = cr.chat_room_id LEFT JOIN users u ON u.user_id = c.user_id WHERE cr.chat_room_id = ?");
        $stmt->bind_param("i", $chat_room_id);
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }

    /**
     * Checking for duplicate user by email address
     * @param String $email email to check in db
     * @return boolean
     */
    private function isUserExists($email) {
        $stmt = $this->conn->prepare("SELECT user_id from users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }


    /**
     * 생성되어진 채팅방이 중복인지에 대해서 확인해주는 클래스.
     * @param String 파라미터 값으로 name과 product_name을 참조 한다.
     * @return boolean
     */
    private function isChatroomExists($name, $product_name) {
        $stmt = $this->conn->prepare("SELECT chat_room_id from  WHERE name = ? AND WHERE product_name = ?"); //같은 이름의 같은 제품명이 있는경우에는 중복으로 취급을 하도록한다.
        $stmt->bind_param("ss", $name,$product_name);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0; //true false 반환.
    }


    /**
     * Fetching user by email
     * @param String $email User email id
     */
    public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT user_id, name, email, created_at FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            // $user = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($user_id, $name, $email, $created_at);
            $stmt->fetch();
            $user = array();
            $user["user_id"] = $user_id;
            $user["name"] = $name;
            $user["email"] = $email;
            $user["created_at"] = $created_at;
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }

}

?>
