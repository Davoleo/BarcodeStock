::Coded By Davoleo
::Davoleo - (c) - 2019
@ECHO OFF

echo Requesting Admin Privileges...
if not "%1"=="am_admin" (powershell start -verb runas '%0' am_admin & exit /b)

echo WELCOME TO MY HYPERVISOR SCRIPT
echo Enter 'off' to disable automatic hypervisor launch
echo Enter 'auto' to enable automatic hypervisor launch
echo Quit the Terminal to Cancel this operation

SET /p MODE="Enter your choice: "

bcdedit /set hypervisorlaunchtype %MODE%

echo press enter to restart your computer automatically
echo if you want to restart manually later quit the terminal
PAUSE

shutdown /r /c "Your computer is going to be restarted for hypervisor launch settings to take effect"