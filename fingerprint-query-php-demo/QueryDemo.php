<?php
/** 产品密钥ID，产品标识 */
define("SECRETID", "your secret id");
/** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
define("SECRETKEY", "your secret key");
/** 业务ID，易盾根据产品业务特点分配 */
define("BUSINESSID", "your business id");
/** 易盾短信服务发送接口地址 */
define("API_URL", "https://fp-query.dun.163.com/v1/device/query");
/** api version */
define("VERSION", "v1");
/** php内部使用的字符串编码 */
define("INTERNAL_STRING_CHARSET", "auto");
/**
 * 计算参数签名
 * $params 请求参数
 * $secretKey secretKey
 */
function gen_signature($secretKey, $params)
{
    ksort($params);
    $buff = "";
    foreach ($params as $key => $value) {
        if ($value !== null) {
            $buff .= $key;
            $buff .= $value;
        }
    }
    $buff .= $secretKey;
    return md5($buff);
}
/**
 * 将输入数据的编码统一转换成utf8
 * @params 输入的参数
 */
function toUtf8($params)
{
    $utf8s = array();
    foreach ($params as $key => $value) {
        $utf8s[$key] = is_string($value) ? mb_convert_encoding($value, "utf8", INTERNAL_STRING_CHARSET) : $value;
    }
    return $utf8s;
}
/**
 * 易盾本机验证在线检测请求接口简单封装
 * $params 请求参数
 */
function check($params)
{
    $params["secretId"] = SECRETID;
    $params["businessId"] = BUSINESSID;
    $params["version"] = VERSION;
    $params["timestamp"] = sprintf("%d", round(microtime(true) * 1000));
    // time in milliseconds
    $params["nonce"] = substr(md5(time()), 0, 32);
    $params["token"] = "wefsdf";
    // random int
    $params = toUtf8($params);
    $params["signature"] = gen_signature(SECRETKEY, $params);

    $jsonStr = json_encode($params);
    $result = http_post_json(API_URL,$jsonStr);
    return $result;
}

function http_post_json($url, $jsonStr)
{
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonStr);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/json; charset=utf-8',
            'Content-Length: ' . strlen($jsonStr)
        )
    );
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return array($httpCode, $response);
}



// 简单测试
function main()
{
    $params = array();
    $ret = check($params);
    if($ret[0] == 200) {
        print_r($ret[1]);
    }
}
main();