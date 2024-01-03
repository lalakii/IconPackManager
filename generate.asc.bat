@echo off
set ver=4.3
echo My AAR Publish Script.
set wd=%~dp0
set aar_dir="%wd%library\build\outputs\aar\"
set aar_file="com.iamverycute.iconpackmanager-release.aar"
set aar_path=%aar_dir:"=%%aar_file:"=%
if exist %aar_dir% (
	echo "Generate ASC"
	gpg --yes --armor --detach-sign IconPackManager-%ver%.pom
        gpg --yes --armor --detach-sign %aar_path%
) else (
	echo "aar file not found!"
)

pause