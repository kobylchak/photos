<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
    <title>ua</title>
</head>
<body>
<div align="center">
    <form action="/view" method="POST">
        Photo id: <input type="text" name="photo_id">
        <input type="submit" value="View photo by id"/>
    </form>
    <form action="/add_photo" enctype="multipart/form-data" method="POST">
        Photo: <input type="file" name="photo">
        <input type="submit" value="Load photo"/>
    </form>
    <input type="submit" value="View all photos" onclick="window.location='/view_all';">
    <form action="download_photos" method="post">
        <table border="1">
            <c:forEach items="${photos}" var="photo_id">
                <tr>
                    <td><input type="checkbox" name="toDelete[]" value="${photo_id}" id="checkbox_${photo_id}"></td>
                    <td><a href="/photo/${photo_id}">${photo_id}</a></td>
                    <td><img src="/photo/${photo_id}"
                             height="100" alt="${photo_id}"/>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${photos ne null}">
            <button type="button" id="delete_photos">Delete images</button>
            <button type="submit">Download zip</button>
        </c:if>
    </form>
</div>
<script type="text/javascript">
    $('#delete_photos').click(function () {
        var data = {'toDelete[]': []};
        $(":checked").each(function () {
            data['toDelete[]'].push($(this).val());
        });
        $.post("/photos/delete", data, function (data, status) {
            window.location.reload();
        });
    });
</script>
</body>
</html>
