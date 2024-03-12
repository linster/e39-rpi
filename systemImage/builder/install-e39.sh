#!/bin/bash -ex

echo "Installer stub script"


cd /var/lib/e39
sudo chmod -R 0777 /var/lib/e39/

zip -d /var/lib/e39/e39Rpi-linux-x64-1.0.0.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

#TODO clean up the setup scripts with sensitive info.

username=`cat /var/lib/e39/setup/username`
cp /var/lib/e39/setup/.jwmrc /home/${username}/.jwmrc


systemctl enable vncserver-virtuald.service


#Setup auto-login
sudo raspi-config nonint do_boot_behaviour B4

#Turn off screen blanking
sudo raspi-config nonint do_blanking 1

#Enable i2c for relays
sudo raspi-config nonint do_i2c 0

# From https://github.com/JetBrains/skiko/issues/649
# We need to put this
# export MESA_EXTENSION_OVERRIDE="-GL_ARB_invalidate_subdata"
# into whatever sets up the environment variables for the user

echo "export MESA_EXTENSION_OVERRIDE=\"-GL_ARB_invalidate_subdata\"" >> /home/${username}/.xsessionrc

cat << EOF > /var/lib/e39/theme.conf
themeConfig {
    "Theme setting for windowsize takes precedence over main conf file"=true
    themeName=BmwBlueNormalSize
}
EOF

chmod 0777 /var/lib/e39/theme.conf

cat << EOF > /home/${username}/e39.sh
#!/bin/bash
export MESA_EXTENSION_OVERRIDE="-GL_ARB_invalidate_subdata" 
java -jar /var/lib/e39/e39Rpi-linux-x64-1.0.0.jar > /home/${username}/hmi.log
EOF

chmod 0777 /home/${username}/e39.sh
chmod +x /home/${username}/e39.sh

