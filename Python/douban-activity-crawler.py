#coding=utf8
import httplib2
from BeautifulSoup import BeautifulSoup
from datetime import *
import random
import time
from fake_useragent import UserAgent

def processPage(head, content):
    soup = BeautifulSoup(content)
    acList = soup.find("ul", {"class" : "events-list events-list-pic100 events-list-psmall"}).findAll("li", {"class" : "list-entry"})
    for ac in acList:
        info = {}
        info['title'] = ac.find("div", {"class" : "title"}).find('a')['title']
        info['startTime'] = ac.find("time", {"itemprop" : "startDate"})['datetime']
        info['endTime'] = ac.find("time", {"itemprop" : "endDate"})['datetime']
        info['location'] = ac.find("meta", {"itemprop" : "location"})['content']
        info['fee'] = ac.find("li", {"class" : "fee"}).strong.string
        info['joined'] = ac.find("p", {"class" : "counts"}).findAll("span")[0].string
        info['interested'] = ac.find("p", {"class" : "counts"}).findAll("span")[2].string
        #for key in info:
            #print info[key]

acTypes = ['music', 'drama', 'salon', 'party', 'film', 'exhibition', 'commonweal', 'travel', 'sports', 'others']
h = httplib2.Http(".cache")
urlTemplate = "http://shanghai.douban.com/events/%s-%s?start=%d"
headers = {
    'Accept': 'application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5',
    'Accept-Charset': 'UTF-8,*;q=0.5',
    'Accept-Encoding': 'gzip,deflate,sdch',
    'Accept-Language': 'zh-CN,zh;q=0.8',
    'Cache-Control': 'max-age=0',
    'Connection': 'keep-alive',
    #'User-Agent': 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.24',
}
endDate = datetime(2010, 12, 31)
startDate = datetime(2010, 4, 26)
activityIds = []
ua = UserAgent()
while(startDate != endDate):
    startDate = startDate + timedelta(-1)
    strDate = startDate.strftime("%Y%m%d")
    for curType in acTypes:
        page = 0
        while(1):
            url = urlTemplate % (strDate,curType,page)
            page += 10
            print url
            #headers['User-Agent'] = random.choice(user_agent_list)
            headers['User-Agent'] = ua.random
            resp, content = h.request(url, headers=headers)
            print resp.status
            if (resp.status != 200):
                f = open("errors", "a+")
                f.write(url)
                f.write("\n")
                f.write("%d" % resp.status)
                f.write("\n")
                f.close()
            soup = BeautifulSoup(content)
            eventList = soup.find("ul", {"class" : "events-list events-list-pic100 events-list-psmall"})
            if (eventList == None):
                break
            filename = ".data/2010/%s-%s-%d" % (strDate, curType, page)
            f = open(filename, "w")
            content = "".join([str(item) for item in eventList.contents])
            f.write(content)
            f.close()
            time.sleep(1)

#processPage(resp, content)