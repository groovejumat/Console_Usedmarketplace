<?php
      
      $conn = mysqli_connect("localhost", "root", "server114##$$", "gcm_chat");
      mysqli_set_charset($conn,'utf8');
      $insert_query = "INSERT INTO product (productname, price, description, imagelist) VALUES ('ㅎㅇㅎㅇ','qqq','qqq','qqq')";
      mysqli_query($conn, $insert_query);
?>



