This file is to document any internal things Questlog does and how Questlog is built.

Questlog is a Forge mod. To modify any dependency versions or mappings, refer to build.properties, don't change them in build.gradle.

Quest syncing and managing is done on the server.
Both the logical client and logical server keep a copy of QuestManager (server actually holds one for each player).
Client keeps a copy of QuestManager in QuestManager.INSTANCE (error thrown when getting instance on server),
server keeps them in ServerPlayerManager (ServerPlayerManager.INSTANCE) (instance is always null on client).

The core subpackage contains mod-specific data structures and classes essential to the core function of the mod. This does not include stuff like configs, guis, packet handlers, game registries, events, etc.

#### **NbtSaveable**
An interface that allows an object to be saved to NBT. It has two methods, `writeInitialData`, `serialize`, and `deserialize`.
The method `writeInitialData` is called when the object is first created, and `serialize` and `deserialize` are called when the object is saved and loaded from NBT.
The method `deserialize` is also called with the same object which `writeInitialData` was called with, so it should be able to handle that.

For more information, see the Javadoc of the interface.

#### **WithDisplayData**
An interface that allows an object to have display data. Display data is data that is used to display the object in a GUI or other visual representation.

#### **Quest** implements [NbtSaveable](#NbtSaveable), [WithDisplayData](#WithDisplayData)
Data structure representing a quest. It holds a list of triggers, objectives, and rewards.

#### **Objective** implements [NbtSaveable](#NbtSaveable), [WithDisplayData](#WithDisplayData)
Data structure representing an objective. (see wiki for what it does). Triggers are also objectives, they just don't have display data.
The superclass must listen to events on the forge event bus to update its state, when it does,
the base class will notify the quest class (which in turn notifies the manager), which then syncs the data to the client.

#### **Reward** implements [NbtSaveable](#NbtSaveable), [WithDisplayData](#WithDisplayData)
Data structure representing a reward. (see wiki for what it does)
Tracks whether the reward has been claimed or not, as well as handles the actual rewarding.
