call "make.bat"

copy CFX_ImageInfo.jar C:\ColdFusion10\cfusion\cfx\java

rem net stop "ColdFusion MX Application Server"
net stop "ColdFusion 10 Application Server"
net stop w3svc
net stop "WAS" /Y

rem net start "IIS Admin Service"
net start "WAS"
net start w3svc
rem net start "ColdFusion MX Application Server"
net start "ColdFusion 10 Application Server"

pause
