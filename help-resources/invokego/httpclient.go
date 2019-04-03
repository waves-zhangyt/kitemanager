package main

import (
	"crypto/md5"
	"encoding/hex"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"time"
)

func main()  {

	client := &http.Client{}
	req, err := http.NewRequest("GET", "http://localhost:8899/kite/", nil)
	if err != nil {
		log.Println(err)
	}

	appId := "yt"
	secret := "yt"
	timestamp := int(time.Now().Unix())
	feed := appId + "-" + secret + "-" + strconv.Itoa(timestamp)
	tokenData := md5.Sum([]byte(feed))
	req.Header.Add("kAppId", appId)
	req.Header.Add("kAppToken", hex.EncodeToString(tokenData[:]))
	req.Header.Add("kTimestamp", strconv.Itoa(timestamp))

	resp, err := client.Do(req)
	defer resp.Body.Close()

	//打印返回的body信息
	body, err1 := ioutil.ReadAll(resp.Body)
	if err1 != nil {
		log.Println(err)
	}

	fmt.Println(string(body))

}
