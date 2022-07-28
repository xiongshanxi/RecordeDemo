from bs4 import BeautifulSoup
import requests
import numpy as np

# 爬取网页并解析
def get_http():
    requests.packages.urllib3.disable_warnings()
    r = requests.get("https://www.baidu.com/",verify=False)
    r.encoding ='utf-8'
    bsObj = BeautifulSoup(r.text,"html.parser")
    for node in bsObj.findAll("a"):
        print("---**--- ", node.text)

# 使用numpy
def print_numpy():
    y = np.zeros((5,), dtype = np.int)
    print(y)

def Love():
    print('\n'.join([''.join([(''[(x-y) % len('wwb_nb')] if ((x*0.05)**2+(y*0.1)**2-1)**3-(x*0.05)**2*(y*0.1)**3 <= 0else' ') for x in range(-30, 30)]) for y in range(30, -30, -1)]))
