from __future__ import annotations

from typing import Generic, Iterator, TypeVar

T = TypeVar("T")


class ListNode(Generic[T]):
    __slots__ = ("value", "next")

    def __init__(self, value: T) -> None:
        self.value = value
        self.next: ListNode[T] | None = None


class LinkedList(Generic[T]):
    """Liste chaînée simple pour les enfants retournés par State.expand."""

    def __init__(self) -> None:
        self.head: ListNode[T] | None = None
        self.tail: ListNode[T] | None = None

    def append(self, item: T) -> None:
        node: ListNode[T] = ListNode(item)
        if self.tail is None:
            self.head = self.tail = node
        else:
            self.tail.next = node
            self.tail = node

    def __iter__(self) -> Iterator[T]:
        n = self.head
        while n is not None:
            yield n.value
            n = n.next
