//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds all available information about a Thread.
 *
 * @property contextId                 ID of the Thread's Context.
 * @property threadId                  ID of the Thread.
 * @property createDate                Thread creation timestamp.
 * @property creator                   ID of the user who created the Thread.
 * @property lastModificationDate      Thread last modification timestamp.
 * @property lastModifier              ID of the user who last modified the Thread.
 * @property users                     List of users (their IDs) with access to the Thread.
 * @property managers                  List of users (their IDs) with management rights.
 * @property version                   Version number (changes on updates).
 * @property lastMsgDate               Timestamp of the last posted message.
 * @property publicMeta                Thread's public metadata.
 * @property privateMeta               Thread's private metadata.
 * @property policy                    Thread's policies
 * @property messagesCount             Total number of messages in the Thread.
 * @property statusCode                Status code of retrieval and decryption of the `Thread`.
 *
 * @category thread
 * @group Thread
 */
class Thread(
    var contextId: String?,
    var threadId: String?,
    var createDate: Long?,
    var creator: String?,
    var lastModificationDate: Long?,
    var lastModifier: String?,
    var users: List<String?>?,
    var managers: List<String?>?,
    var version: Long?,
    var lastMsgDate: Long?,
    var publicMeta: ByteArray?,
    var privateMeta: ByteArray?,
    var policy: ContainerPolicy?,
    var messagesCount: Long?,
    var statusCode: Long?
)