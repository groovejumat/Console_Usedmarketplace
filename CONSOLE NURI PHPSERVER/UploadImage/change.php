<?php
// Path to move uploaded files
$target_path = dirname(__FILE__).'/uploads/';
$dbimagelist = $_POST['notfilepart'];

$product_id=$_POST['productidpart'];
$size = $_POST['size']; //size라는 이름으로 요청을 post로 받아온다.
$theme = $_POST['themepart'];
$price = $_POST['pricepart'];
$category = $_POST['category']; //카테고리 값 추가.
$description = $_POST['descriptionpart'];

// $myfile=fopen($target_path."newfile.txt","w") or die("Unable to open file");
//
// // fwrite($myfile,$dbimagelist);
// // fwrite($myfile,$theme);
// // fwrite($myfile,$price);
// // fwrite($myfile,$description);
//
// fwrite($myfile,$size);
// fwrite($myfile,$theme);
// fwrite($myfile,$price);
// fwrite($myfile,$dbimagelist);
//
// fclose($myfile);

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

    // $conn = mysqli_connect("localhost", "root", "server114##$$", "gcm_chat");
    // mysqli_set_charset($conn,'utf8');
    //
    // //이부분에서 정보를 교체하는 쿼리를 실행하도록합니다.
    // $insert_query = "INSERT INTO product (productname, price, description, imagelist) VALUES ('$theme','$price','$description','$dbimagelist')";
    // mysqli_query($conn, $insert_query);



} else {
    // File parameter is missing
    echo json_encode(array('status'=>'fail', 'message'=>'Not received any file'));
}

$myfile=fopen($target_path."newfile.txt","w") or die("Unable to open file");

fwrite($myfile,$product_id);
fwrite($myfile,$size);
fwrite($myfile,$theme);
fwrite($myfile,$price);
fwrite($myfile,$dbimagelist);

//해당 부분에서 db변경 처리를 해주어야 한다.
$conn = mysqli_connect("localhost", "root", "server114##$$", "gcm_chat");
mysqli_set_charset($conn,'utf8');

//이부분에서 정보를 교체하는 쿼리를 실행하도록합니다.
$insert_query = "UPDATE product SET productname='$theme',price='$price',description='$description',imagelist='$dbimagelist', category='$category' WHERE product_id='$product_id'";

fwrite($myfile,$insert_query);
fclose($myfile);

mysqli_query($conn, $insert_query);

?>
