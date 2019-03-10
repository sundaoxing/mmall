<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<html>
<body>
<h2>Hello World!</h2>


springMVC上传文件
<form name="uploadFile" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="上传" />
</form>

springMVC富文本上传
<form name="uploadRichText" action="/manage/product/rich_text_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_rich_text" />
    <input type="submit" value="上传" />
</form>
</body>
</html>
