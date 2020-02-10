from PIL import Image 
import pytesseract
import requests
from bs4 import BeautifulSoup
import urllib.request


page = requests.get("https://www.saskatchewan.ca/government/government-structure/ministries/health/other-reports/influenza-reports")
soup = BeautifulSoup(page.content)
for res in soup.findAll("section", {"class": "general-content"})[0].findAll('img'):
	report_img = res.get('src')
	print(res.get('src'))


urllib.request.urlretrieve(report_img, "./local-filename.png")

im = Image.open("./local-filename.png") 

left = 650
top = 217
right = 900
bottom = 275

im1 = im.crop((left, top, right, bottom)) 

left = 77
top = 217
right = 164
bottom = 275
im2 = im.crop((left, top, right, bottom)) 

im1.show()
im2.show() 

print(pytesseract.image_to_string(im1))
print(pytesseract.image_to_string(im2))