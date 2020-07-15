<?php
// Path to move uploaded files
$target_path = dirname(__FILE__).'/uploads/';
$dbimagelist = "";

$userid = $_POST['userid']; //작성자의 고유 아이디 값 또한 가지고 온다.
$username = $_POST['username'];
$size = $_POST['size']; //size라는 이름으로 요청을 post로 받아온다.
$theme = $_POST['themepart'];
$price = $_POST['pricepart'];
$category = $_POST['category'];
$description = $_POST['descriptionpart'];

if (!empty($_FILES)) {
    for ($x = 0; $x < $size; $x++) {
        try {
            //$newname = basename( $_FILES["fileToUpload"]["name"]);
            $newname = date('YmdHis',time()).mt_rand().'.jpg';

            $dbimagelist.=$newname." ";
            //$dbimagelist =. $newname. " ";
            // Throws exception incase file is not being moved
            // $target_path .$newname
            if (!move_uploaded_file($_FILES['image'.$x]['tmp_name'], $target_path .$newname)) {
                // make error flag true
                echo json_encode(array('status'=>'fail', 'message'=>'could not move file'));
            }
            // File successfully uploaded //이 부분에서 데이터베이스 등록작업을 실행해주어야 한다.
            echo json_encode(array('status'=>'success', 'message'=>'File Uploaded'));
        } catch (Exception $e) {
            // Exception occurred. Make error flag true
            echo json_encode(array('status'=>'fail', 'message'=>$e->getMessage()));
        }
    }

    $conn = mysqli_connect("localhost", "root", "server114##$$", "gcm_chat");
    mysqli_set_charset($conn,'utf8');
    $insert_query = "INSERT INTO product (productname, price, description, imagelist, category, writer, userid) VALUES ('$theme','$price','$description','$dbimagelist','$category','$username','$userid')";
    mysqli_query($conn, $insert_query);


    $myfile=fopen($target_path."newfile.txt","w") or die("Unable to open file");
    // fwrite($myfile,$size);
    // fwrite($myfile,$name);
    //해당 내용들이 잘 가지고 와졌는지에 대해서 확인
    fwrite($myfile,$dbimagelist);
    fwrite($myfile,$theme);
    fwrite($myfile,$price);
    fwrite($myfile,$description);
    fclose($myfile);


} else {
    // File parameter is missing
    echo json_encode(array('status'=>'fail', 'message'=>'Not received any file'));
}
?>
