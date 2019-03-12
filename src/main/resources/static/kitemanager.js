/** 获取url查询参数 */
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}
/** 获取url是否有查询参数 */
function existsQueryString(name) {
    var url = location.search;
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for(var i = 0; i < strs.length; i++) {
            if (strs[i].split("=")[0] == name) {
                return true;
            }
        }
    }
    else {
        return false;
    }
}