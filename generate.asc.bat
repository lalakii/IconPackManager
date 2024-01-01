@echo off
echo My AAR Publish Script.
set wd=%~dp0
set aar_dir="%wd%library\build\outputs\aar\"
set aar_file="com.iamverycute.iconpackmanager-release.aar"
set aar_path=%aar_dir:"=%%aar_file:"=%
if exist %aar_dir% (
	echo "Generate ASC"
	gpg --armor --detach-sign IconPackManager-4.0.pom
      gpg --armor --detach-sign %aar_path%
) else (
	echo "file not found!"
)

pause