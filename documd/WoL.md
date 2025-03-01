# Wake-on-LAN

## Linux

### Check whether the Wake-on-LAN is enabled

```bash
ethtool eth0 | grep Wake-on
```

#### Success response

If the WoL was enabled the response contains `Wake-on: g`, instead whether the response containto `Wake-on: d` you need
to enable it with [this](#enable-the-wol) command

#### Failed response

If the response is similar to this:

```bash
netlink error: no device matches name (offset 24)
netlink error: No such device
netlink error: no device matches name (offset 24)
netlink error: No such device
netlink error: no device matches name (offset 24)
netlink error: No such device
netlink error: no device matches name (offset 24)
netlink error: No such device
netlink error: no device matches name (offset 24)
```

Execute the one of following commands to see what are the available network interfaces:

```bash
ip link show

#or

ifconfig -a
```

Then try to use the interfaces available instead of `eth0`, for example `eth1`, etc...

The rest of the documentation will use as default the `eth0` as interface, but you need to change with the selected
one if you need to change that value.

### Enable the WoL

```bash
sudo ethtool -s eth0 wol g
```

### Make this option permanently enabled

To make this option permanently enabled you need to add this line:

```bash
ethtool -s eth0 wol g
```

to one file of the listing ones below:

- `/etc/network/interfaces`
- `/etc/rc.local`