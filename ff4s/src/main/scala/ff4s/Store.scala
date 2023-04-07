/* Copyright 2022 buntec
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ff4s

import cats.effect.implicits._
import cats.effect.kernel.Concurrent
import cats.effect.kernel.Resource
import fs2.concurrent.Signal
import fs2.concurrent.SignallingRef
import cats.effect.kernel.Async
import org.http4s.Uri

trait Store[F[_], State, Action] {

  def dispatch(action: Action): F[Unit]

  def state: Signal[F, State]

}

object Store {

  def apply[F[_]: Concurrent, State, Action](
      initialState: State
  )(
      makeDispatcher: SignallingRef[F, State] => Action => F[Unit]
  ): Resource[F, Store[F, State, Action]] = for {

    state0 <- SignallingRef.of[F, State](initialState).toResource

    dispatcher = makeDispatcher(state0)

  } yield (new Store[F, State, Action] {

    override def dispatch(action: Action): F[Unit] = dispatcher(action)

    override def state: Signal[F, State] = state0

  })

  def withRouter[F[_], State, Action](initialState: State)(
      onUriChange: Uri => Action
  )(
      makeDispatcher: (
          SignallingRef[F, State],
          Router[F]
      ) => Action => F[Unit]
  )(implicit F: Async[F]) = for {

    state0 <- SignallingRef.of[F, State](initialState).toResource

    history = fs2.dom.Window[F].history[Unit]

    router <- Router[F](history)

    dispatcher = makeDispatcher(state0, router)

    _ <- router.location.discrete
      .evalMap(uri => dispatcher(onUriChange(uri)))
      .compile
      .drain
      .background

  } yield new Store[F, State, Action] {

    override def dispatch(action: Action): F[Unit] = dispatcher(action)

    override def state: Signal[F, State] = state0

  }

}
