# Configurations

### Semux Core configuration

```
################################################################################
#                                                                              #
# Copyright (c) 2017-2018 The Semux Developers                                 #
#                                                                              #
# Distributed under the MIT software license, see the accompanying file        #
# LICENSE or https://opensource.org/licenses/mit-license.php                   #
#                                                                              #
################################################################################

#================
# P2P
#================

# Declared ip address
p2p.declaredIp =

# Binding IP address and port
p2p.listenIp = 0.0.0.0
p2p.listenPort = 5161

# Seed nodes, IP addresses separated by comma
p2p.seedNodes =


#================
# Network
#================

# Max number of inbound connections
net.maxInboundConnections = 1024

# Max number of inbound connections from each unique IP address
net.maxInboundConnectionsPerIp = 5

# Max number of outbound connections
net.maxOutboundConnections = 128

# Max message queue size
net.maxMessageQueueSize = 4096

# Message relay redundancy
net.relayRedundancy = 8

# Channel idle timeout, ms
net.channelIdleTimeout = 120000

# DNS Seed (comma delimited)
net.dnsSeeds.mainNet = mainnet.semux.org, mainnet-seed.semux.info
net.dnsSeeds.testNet = testnet.semux.org, testnet-seed.semux.info

#================
# API
#================

# Whether to enable API services. Make sure you change the password below.
api.enabled = false

# Binding IP address and port
api.listenIp = 127.0.0.1
api.listenPort = 5171

# Basic access authentication credential
api.username = YOUR_API_USERNAME
api.password = YOUR_API_PASSWORD

# Enable services below as public (authentication optional)
api.public = blockchain,account,delegate,tool

# Enable services below as private (authentication required)
api.private = node,wallet

#================
# UI
#================

# Specify the localization of UI
# ui.locale = en_US

# Specify the unit & fraction digits of values
# ui.unit must be one of SEM, mSEM, ??SEM
ui.unit = SEM
ui.fractionDigits = 9

#================
# Transaction pool
#================

# The max total gas consumed for block proposals
txpool.maxTotalGasConsumed = 10000000

# The max transaction gasLimit accepted
txpool.maxTxGasLimit = 5000000

# The minimum price this client will accept for gas
txpool.minTxGasPrice = 100

# The max transaction time drift in milliseconds
txpool.maxTxTimeDrift = 7200000

#================
# Syncing
#================

# Disconnect the peer when an invalid block is received
sync.disconnectOnInvalidBlock = false

# Use the FAST_SYNC protocol, experimental
sync.fastSync = true
```

### IP whitelist and blacklist

Example `ipfilter.json`:
```
{
    "rules": [
        {"type": "ACCEPT", "address": "127.0.0.1/8"},
        {"type": "ACCEPT", "address": "192.168.0.0/16"},
        {"type": "REJECT", "address": "8.8.8.8"}
    ]
}
```
