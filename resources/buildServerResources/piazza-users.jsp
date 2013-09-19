
<div class="Portraits">
            <c:forEach var="user" items="${build.users}">
                <div class="Portrait">
                <c:choose>
                	<c:when test="${empty user.portraitURL}"><img src="<%= request.getContextPath() %>${resourceRoot}silhouette.jpg" title="${fn:escapeXml(user.name)}"></c:when>
                	<c:otherwise><img src="${fn:escapeXml(user.portraitURL)}" title="${fn:escapeXml(user.name)}"></c:otherwise>
                </c:choose>
                <p class="Name">${user.login} - ${user.name}</p>
                </div>
            </c:forEach>
</div>