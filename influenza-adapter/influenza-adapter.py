from PIL import Image 
import pytesseract
import requests
from bs4 import BeautifulSoup
import urllib.request
import time
from datetime import datetime
import os
from collections import namedtuple
import redis

def get_count():
	page = requests.get("https://www.saskatchewan.ca/government/government-structure/ministries/health/other-reports/influenza-reports")
	soup = BeautifulSoup(page.content)
	for res in soup.findAll("section", {"class": "general-content"})[0].findAll('img'):
		report_img = res.get('src')
		print(res.get('src'))


	file_name = "./Report-{0}-{1}".format(datetime.now().strftime("%Y-%m-%d-%H-%M-%S"), os.path.basename(report_img)) 
	urllib.request.urlretrieve(report_img, file_name)

	report_full_img = Image.open(file_name) 

	week_left = 50
	week_top = 105
	week_right = 600
	week_bottom = 150

	week_img = report_full_img.crop((week_left, week_top, week_right, week_bottom)) 

	total_left = 650
	total_top = 217
	total_right = 900
	total_bottom = 275

	total_img = report_full_img.crop((total_left, total_top, total_right, total_bottom)) 

	new_left = 77
	new_top = 217
	new_right = 164
	new_bottom = 275
	new_img = report_full_img.crop((new_left, new_top, new_right, new_bottom)) 

	total_img.save("./Total-{0}-{1}".format(datetime.now().strftime("%Y-%m-%d-%H-%M-%S"), os.path.basename(report_img)) )
	new_img.save("./New-{0}-{1}".format(datetime.now().strftime("%Y-%m-%d-%H-%M-%S"), os.path.basename(report_img)) ) 

	week = pytesseract.image_to_string(week_img)
	total_count = pytesseract.image_to_string(total_img)
	new_count = pytesseract.image_to_string(new_img)

	print("{} {} Total Cases: {}".format(datetime.now().strftime("%Y-%m-%d %H:%M:%S"), week, total_count))
	print("{} {} New Cases: {}".format(datetime.now().strftime("%Y-%m-%d %H:%M:%S"), week, new_count))

	record = namedtuple("record", ["week", "total", "new"])
	return record(week, total_count, new_count)


if __name__ == "__main__":
	host = "localhost"
	port  = 6379
	if (len(sys.argv) >= 1):
		host = sys.argv[1]
	if (len(sys.argv) >= 2):
		port = int(sys.argv[2])

	redis_client = redis.Redis(host=host, port=port, db=0)
	weeks = set()
	while True:
		r = get_count()
		if (r.week not in weeks):
			with open("./records.csv", 'a') as the_file:
				the_file.write("\"{}\", \"{}\", \"{}\"\n".format(r.week, r.total, r.new))
			redis_client.publish("Influenza-SK", r.new)
			weeks.add(r.week)

		time.sleep(2)