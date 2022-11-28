/*
@Author : yidun_dev
@Date : 2022-11-28
@File : query.go
@Version : 1.0
@Golang : 1.19.3
@Doc : https://support.dun.163.com/documents/609099986339037184?docId=624010587874123776
*/
package main

import (
	"bytes"
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math/rand"
	"net/http"
	"sort"
	"strconv"
	"time"

	simplejson "github.com/bitly/go-simplejson"
)

const (
	apiUrl     = "https://fp-query.dun.163.com/v1/device/query"
	version    = "v1"
	secretId   = "YOUR_SECRET_ID"   //产品密钥ID，产品标识
	secretKey  = "YOUR_SECRET_KEY"  //产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
	businessId = "YOUR_BUSINESS_ID" //业务ID，易盾根据产品业务特点分配
)

// 请求易盾接口
func query(token string) *simplejson.Json {
	params := make(map[string]string)
	params["token"] = token
	params["nonce"] = strconv.FormatInt(rand.New(rand.NewSource(time.Now().UnixNano())).Int63n(10000000000), 10)
	params["timestamp"] = strconv.FormatInt(time.Now().UnixNano()/1000000000, 10)
	params["secretId"] = secretId
	params["version"] = version
	params["businessId"] = businessId
	params["signature"] = genSignature(params)

	byteParams, err := json.Marshal(params)
	if err != nil {
		fmt.Println(err.Error())
		return nil
	}
	resp, err := http.Post(apiUrl, "application/json;charset=utf-8", bytes.NewReader(byteParams))

	if err != nil {
		fmt.Println("调用API接口失败:", err)
		return nil
	}

	defer resp.Body.Close()

	contents, _ := ioutil.ReadAll(resp.Body)
	result, _ := simplejson.NewJson(contents)
	return result
}

// 生成签名信息
func genSignature(params map[string]string) string {
	var paramStr string
	keys := make([]string, 0, len(params))
	for k := range params {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	for _, key := range keys {
		if len(params[key]) == 0 {
			paramStr += key + ""
		} else {
			paramStr += key + params[key]
		}
	}
	paramStr += secretKey
	md5Reader := md5.New()
	md5Reader.Write([]byte(paramStr))
	return hex.EncodeToString(md5Reader.Sum(nil))

}

func main() {
	//传入上传设备数据时，设备指纹SDK返回的token
	ret := query("YOUR_TOKEN")

	code, _ := ret.Get("code").Int()
	message, _ := ret.Get("msg").String()
	if code == 200 {
		res, err := ret.Get("data").MarshalJSON()
		if err != nil {
			fmt.Println(err.Error())
		}
		fmt.Println("接口调用成功" + string(res))
	} else {
		fmt.Printf("ERROR: code=%d, msg=%s", code, message)
	}
}
