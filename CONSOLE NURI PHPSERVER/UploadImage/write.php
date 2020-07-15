<?php    
$target_path = dirname(__FILE__).'/uploads/';

    $myfile=fopen($target_path."newfile.txt","w") or die("Unable to open file");
    // fwrite($myfile,$size);
    // fwrite($myfile,$name);
    //해당 내용들이 잘 가지고 와졌는지에 대해서 확인
    // fwrite($myfile,$dbimagelist);
    // fwrite($myfile,$theme);
    // fwrite($myfile,$price);
    // fwrite($myfile,$description);

    fwrite($myfile,"아아아");
    fwrite($myfile,"아아아");
    fwrite($myfile,"아아아");
    fwrite($myfile,"아아아");


    fclose($myfile);
?>
