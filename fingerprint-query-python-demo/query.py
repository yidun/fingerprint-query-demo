# -*- coding: utf-8 -*-
from hashlib import md5
import json
import random
import time
import requests


class Query(object):
    """易盾设备指纹查询请求示例代码"""
    API_URL = "https://fp-query.dun.163.com/v1/device/query"
    VERSION = "v1"

    def __init__(self, secret_id, secret_key, business_id):
        """
        Args:
            secret_id (str) 产品密钥ID，产品标识
            secret_key (str) 产品私有密钥，服务端生成签名信息使用
            business_id (str) 业务ID，易盾根据产品业务特点分配
        """
        self.secret_id = secret_id
        self.secret_key = secret_key
        self.business_id = business_id

    def _gen_signature(self, params=None):
        """生成签名信息
        Args:
            params (object) 请求参数
        Returns:
            参数签名md5值
        """
        buff = ""
        for k in sorted(params.keys()):
            buff += str(k) + str(params[k])
        buff += self.secret_key
        return md5(buff.encode("utf-8")).hexdigest()

    def send(self, token):
        """请求易盾接口

        Returns:
            请求结果，json格式
            :param token:
        """
        params = {"token": token, "secretId": self.secret_id, "businessId": self.business_id, "version": self.VERSION,
                  "timestamp": int(time.time() * 1000), "nonce": int(random.random() * 100000000)}
        params["signature"] = self._gen_signature(params)

        try:
            headers = {'Content-Type': 'application/json'}
            json_data = json.dumps(params)
            response = requests.post(self.API_URL, data=json_data, headers=headers)
            return json.loads(response.content)
        except Exception as ex:
            print("调用API接口失败:", str(ex))


if __name__ == "__main__":
    """示例代码入口"""
    SECRET_ID = "your_secret_id"  # 产品密钥ID，产品标识
    SECRET_KEY = "your_secret_key"  # 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
    BUSINESS_ID = "your_business_id"  # 业务ID，易盾根据产品业务特点分配
    api = Query(SECRET_ID, SECRET_KEY, BUSINESS_ID)
    token = "your token"
    ret = api.send(token)
    if ret is not None:
        if ret["code"] == 200:
            device = ret["data"]["device"]
            print("deviceId = %s" % device["deviceId"])
            print("checkResult = %s" % device["checkResult"])
        else:
            print ("ERROR: ret.code=%s,msg=%s" % (ret['code'], ret['msg']))
