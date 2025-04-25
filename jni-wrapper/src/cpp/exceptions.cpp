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

#include "exceptions.h"

IllegalStateException::IllegalStateException(const char *message) {
    this->message = message;
}

const char *IllegalStateException::what() const noexcept {
    return this->message;
}


