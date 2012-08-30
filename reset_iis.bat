net stop "IIS Admin Service"
net stop w3svc
net stop "WAS" /Y

net start "IIS Admin Service"
net start "WAS"
net start w3svc
